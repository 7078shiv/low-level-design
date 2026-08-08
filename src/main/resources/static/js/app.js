/*
 * Thin client over the Spring Boot chess API.
 *
 * The server owns every rule. This file only draws the position it is given and
 * posts move attempts; the legal move map that comes with each state lets the board
 * highlight destinations without another round trip.
 */

const API = '/api/chess/games';
const STORAGE_KEY = 'chess.gameId';
const SETUP_KEY = 'chess.setup';

const GLYPHS = { KING: '♚', QUEEN: '♛', ROOK: '♜', BISHOP: '♝', KNIGHT: '♞', PAWN: '♟' };
const VALUES = { QUEEN: 9, ROOK: 5, BISHOP: 3, KNIGHT: 3, PAWN: 1, KING: 0 };
const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];

const DIFFICULTY_NOTE = {
    EASY: 'Looks two moves ahead. It will punish a hanging piece and not much more.',
    MEDIUM: 'Thinks for a second and sees short tactics coming.',
    HARD: 'Thinks for a few seconds, typically ten moves deep. It plays to win.'
};

const el = {
    board: document.getElementById('board'),
    ranks: document.getElementById('ranks'),
    files: document.getElementById('files'),
    stripTop: document.getElementById('strip-top'),
    stripBottom: document.getElementById('strip-bottom'),
    takenTop: document.getElementById('taken-top'),
    takenBottom: document.getElementById('taken-bottom'),
    advTop: document.getElementById('adv-top'),
    advBottom: document.getElementById('adv-bottom'),
    statusTurn: document.getElementById('status-turn'),
    statusNote: document.getElementById('status-note'),
    moveList: document.getElementById('move-list'),
    moveCount: document.getElementById('move-count'),
    promotion: document.getElementById('promotion'),
    promoChoices: document.getElementById('promo-choices'),
    result: document.getElementById('result'),
    resultTitle: document.getElementById('result-title'),
    resultNote: document.getElementById('result-note'),
    setup: document.getElementById('setup'),
    difficultyNote: document.getElementById('difficulty-note'),
    fieldDifficulty: document.getElementById('field-difficulty'),
    fieldSide: document.getElementById('field-side'),
    thinking: document.getElementById('thinking'),
    thinkingText: document.getElementById('thinking-text'),
    toast: document.getElementById('toast')
};

let state = null;
let selected = null;      // origin square while a piece is picked up
let flipped = false;      // black at the bottom
let promotionMove = null; // {from, to} waiting for a piece choice
let resultDismissed = false;
let toastTimer = null;
let thinking = false;     // a bot-move request is in flight

/** Last used setup, reused for a rematch and remembered between visits. */
let setupChoice = loadSetup();

function loadSetup() {
    try {
        const saved = JSON.parse(localStorage.getItem(SETUP_KEY));
        if (saved && saved.opponent) return saved;
    } catch (error) {
        // fall through to the default
    }
    return { opponent: 'BOT', difficulty: 'MEDIUM', side: 'WHITE' };
}

/* ------------------------------------------------------------------ helpers */

const squareName = (row, col) => FILES[col] + (row + 1);
const rowOf = square => Number(square[1]) - 1;
const colOf = square => FILES.indexOf(square[0]);
const legalFrom = square => (state && state.legalMoves[square]) || [];
const titleCase = word => word.charAt(0) + word.slice(1).toLowerCase();

function toast(message) {
    el.toast.textContent = message;
    el.toast.classList.remove('hidden');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.toast.classList.add('hidden'), 2600);
}

async function call(path, options) {
    const response = await fetch(path, options);
    if (response.status === 204) return null;
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.error || 'Request failed');
    return body;
}

/* --------------------------------------------------------------- game setup */

async function newGame(choice = setupChoice) {
    setupChoice = choice;
    localStorage.setItem(SETUP_KEY, JSON.stringify(choice));

    const againstBot = choice.opponent === 'BOT';
    // the bot takes the colour the player did not pick
    const botColour = againstBot ? (choice.side === 'WHITE' ? 'BLACK' : 'WHITE') : null;

    const created = await call(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            whiteName: againstBot && botColour === 'BLACK' ? 'You' : 'White',
            blackName: againstBot && botColour === 'WHITE' ? 'You' : 'Black',
            botColour,
            difficulty: againstBot ? choice.difficulty : null
        })
    });

    remember(created.gameId);
    flipped = againstBot && choice.side === 'BLACK';
    apply(created, { fresh: true });
}

/** Keeps the id in the address bar so a game can be reopened or shared. */
function remember(gameId) {
    localStorage.setItem(STORAGE_KEY, gameId);
    history.replaceState(null, '', `?game=${gameId}`);
}

async function loadOrCreate() {
    const requested = new URLSearchParams(location.search).get('game');
    const saved = requested || localStorage.getItem(STORAGE_KEY);
    if (saved) {
        try {
            apply(await call(`${API}/${saved}`), { fresh: true });
            remember(saved);
            return;
        } catch (error) {
            localStorage.removeItem(STORAGE_KEY);
        }
    }
    await newGame();
}

function apply(next, options = {}) {
    state = next;
    selected = null;
    if (options.fresh) resultDismissed = false;
    render();
    if (state.gameOver && !resultDismissed) showResult();
    if (state.botToMove) requestBotMove();
}

/**
 * The bot is played by the server, one move per request. The player's own move is
 * rendered first so the board never looks frozen while the search runs.
 */
async function requestBotMove() {
    if (thinking) return;
    thinking = true;
    render();
    try {
        const next = await call(`${API}/${state.gameId}/bot-move`, { method: 'POST' });
        thinking = false;
        apply(next);
    } catch (error) {
        thinking = false;
        render();
        toast(error.message);
    }
}

/* ------------------------------------------------------------------- moving */

async function attemptMove(from, to, promotion) {
    try {
        const next = await call(`${API}/${state.gameId}/moves`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ from, to, promotion: promotion || null })
        });
        apply(next);
    } catch (error) {
        selected = null;
        render();
        toast(error.message);
    }
}

/** A pawn reaching the last rank has to say what it becomes before the move is sent. */
function needsPromotion(from, to) {
    const piece = state.board[rowOf(from)][colOf(from)];
    if (!piece || piece.type !== 'PAWN') return false;
    const lastRank = piece.colour === 'WHITE' ? 7 : 0;
    return rowOf(to) === lastRank;
}

function commit(from, to) {
    if (needsPromotion(from, to)) {
        promotionMove = { from, to };
        openPromotion(state.board[rowOf(from)][colOf(from)].colour);
        return;
    }
    attemptMove(from, to);
}

function onSquareClick(square) {
    if (!state || state.gameOver || thinking) return;

    if (selected && legalFrom(selected).includes(square)) {
        const from = selected;
        selected = null;
        commit(from, square);
        return;
    }

    // clicking your own piece picks it up, anything else puts it down
    selected = legalFrom(square).length ? (selected === square ? null : square) : null;
    render();
}

/* ----------------------------------------------------------------- rendering */

function render() {
    if (!state) return;
    renderBoard();
    renderCoordinates();
    renderPlayers();
    renderStatus();
    renderMoves();

    el.board.classList.toggle('waiting', thinking);
    el.thinking.classList.toggle('hidden', !thinking);
    if (thinking) {
        const bot = state.botColour === 'WHITE' ? state.whiteName : state.blackName;
        el.thinkingText.textContent = `${bot} is thinking…`;
    }

    document.getElementById('btn-undo').disabled = !state.canUndo || thinking;
    document.getElementById('btn-resign').disabled = state.gameOver || thinking;
    document.getElementById('btn-draw').disabled = state.gameOver || thinking || !!state.botColour;
    document.getElementById('btn-new').disabled = thinking;
}

function renderBoard() {
    const playable = !state.gameOver && !thinking;
    const destinations = selected ? legalFrom(selected) : [];
    const last = state.lastMove;
    const checkedKing = state.inCheck ? findKing(state.turn) : null;

    el.board.replaceChildren();

    const rows = flipped ? [0, 1, 2, 3, 4, 5, 6, 7] : [7, 6, 5, 4, 3, 2, 1, 0];
    const cols = flipped ? [7, 6, 5, 4, 3, 2, 1, 0] : [0, 1, 2, 3, 4, 5, 6, 7];

    for (const row of rows) {
        for (const col of cols) {
            const square = squareName(row, col);
            const piece = state.board[row][col];

            const cell = document.createElement('div');
            cell.className = `sq ${(row + col) % 2 === 0 ? 'dark' : 'light'}`;
            cell.dataset.square = square;

            if (square === selected) cell.classList.add('selected');
            if (last && (square === last.from || square === last.to)) cell.classList.add('last');
            if (square === checkedKing) cell.classList.add('check');

            const isTarget = destinations.includes(square);
            if (playable && (isTarget || legalFrom(square).length)) cell.classList.add('playable');

            if (piece) {
                const glyph = document.createElement('span');
                glyph.className = `piece ${piece.colour === 'WHITE' ? 'w' : 'b'}`;
                glyph.textContent = GLYPHS[piece.type];
                glyph.draggable = playable && legalFrom(square).length > 0;
                glyph.dataset.square = square;
                cell.appendChild(glyph);
            }

            if (isTarget) {
                if (piece) cell.classList.add('occupied');
                const hint = document.createElement('span');
                hint.className = 'hint';
                cell.appendChild(hint);
            }

            el.board.appendChild(cell);
        }
    }
}

function renderCoordinates() {
    const ranks = flipped ? [1, 2, 3, 4, 5, 6, 7, 8] : [8, 7, 6, 5, 4, 3, 2, 1];
    const files = flipped ? [...FILES].reverse() : FILES;
    el.ranks.replaceChildren(...ranks.map(r => Object.assign(document.createElement('span'), { textContent: r })));
    el.files.replaceChildren(...files.map(f => Object.assign(document.createElement('span'), { textContent: f })));
}

function findKing(colour) {
    for (let row = 0; row < 8; row++) {
        for (let col = 0; col < 8; col++) {
            const piece = state.board[row][col];
            if (piece && piece.type === 'KING' && piece.colour === colour) return squareName(row, col);
        }
    }
    return null;
}

function renderPlayers() {
    const topIsWhite = flipped;
    fillStrip(el.stripTop, el.takenTop, el.advTop, topIsWhite ? 'WHITE' : 'BLACK');
    fillStrip(el.stripBottom, el.takenBottom, el.advBottom, topIsWhite ? 'BLACK' : 'WHITE');
}

function fillStrip(strip, takenNode, advNode, colour) {
    const white = colour === 'WHITE';
    strip.querySelector('.pname').textContent = white ? state.whiteName : state.blackName;
    strip.querySelector('.dot').classList.toggle('white', white);
    strip.classList.toggle('active', !state.gameOver && state.turn === colour);

    // pieces this player has taken belong to the opponent, so they draw in the other colour
    const taken = white ? state.capturedByWhite : state.capturedByBlack;
    takenNode.replaceChildren(...taken.map(type => {
        const span = document.createElement('span');
        span.textContent = GLYPHS[type];
        return span;
    }));

    const mine = sum(white ? state.capturedByWhite : state.capturedByBlack);
    const theirs = sum(white ? state.capturedByBlack : state.capturedByWhite);
    advNode.textContent = mine > theirs ? `+${mine - theirs}` : '';
}

const sum = types => types.reduce((total, type) => total + VALUES[type], 0);

function renderStatus() {
    const line = el.statusTurn;
    line.classList.remove('over', 'check');

    if (state.gameOver) {
        line.textContent = resultHeadline();
        line.classList.add('over');
        el.statusNote.textContent = state.resultReason || '';
        return;
    }

    const name = state.turn === 'WHITE' ? state.whiteName : state.blackName;
    line.textContent = `${name} to move`;
    if (state.inCheck) {
        line.classList.add('check');
        el.statusNote.textContent = 'Check — you must answer it';
    } else {
        el.statusNote.textContent = `Move ${Math.floor(state.history.length / 2) + 1}`;
    }
}

function resultHeadline() {
    switch (state.status) {
        case 'WHITE_WIN': return `${state.whiteName} wins`;
        case 'BLACK_WIN': return `${state.blackName} wins`;
        case 'DRAW': return 'Draw';
        default: return 'Game over';
    }
}

function renderMoves() {
    const history = state.history;
    el.moveCount.textContent = history.length ? `${Math.ceil(history.length / 2)} full moves` : '';

    if (!history.length) {
        el.moveList.replaceChildren(
            Object.assign(document.createElement('li'), { className: 'empty', textContent: 'No moves yet.' }));
        return;
    }

    const rows = [];
    for (let i = 0; i < history.length; i += 2) {
        const item = document.createElement('li');
        item.append(cellSpan('num', `${i / 2 + 1}.`));
        item.append(cellSpan('san', history[i].notation, i === history.length - 1));
        item.append(cellSpan('san', history[i + 1] ? history[i + 1].notation : '', i + 1 === history.length - 1));
        rows.push(item);
    }
    el.moveList.replaceChildren(...rows);
    el.moveList.scrollTop = el.moveList.scrollHeight;
}

function cellSpan(className, text, latest) {
    const span = document.createElement('span');
    span.className = latest ? `${className} latest` : className;
    span.textContent = text;
    return span;
}

/* ---------------------------------------------------------------- promotion */

function openPromotion(colour) {
    el.promoChoices.replaceChildren(...['QUEEN', 'ROOK', 'BISHOP', 'KNIGHT'].map(type => {
        const button = document.createElement('button');
        button.innerHTML = `<span class="piece ${colour === 'WHITE' ? 'w' : 'b'}">${GLYPHS[type]}</span>`;
        button.title = titleCase(type);
        button.onclick = () => {
            const move = promotionMove;
            closePromotion();
            attemptMove(move.from, move.to, type);
        };
        return button;
    }));
    el.promotion.classList.remove('hidden');
}

function closePromotion() {
    promotionMove = null;
    el.promotion.classList.add('hidden');
}

/* ------------------------------------------------------------------- result */

function showResult() {
    el.resultTitle.textContent = resultHeadline();
    el.resultNote.textContent = state.resultReason || '';
    el.result.classList.remove('hidden');
}

/* --------------------------------------------------------------- board input */

el.board.addEventListener('click', event => {
    const cell = event.target.closest('.sq');
    if (cell) onSquareClick(cell.dataset.square);
});

el.board.addEventListener('dragstart', event => {
    const piece = event.target.closest('.piece');
    if (!piece || !piece.draggable) {
        event.preventDefault();
        return;
    }
    selected = piece.dataset.square;
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', selected);
    render();
    // the re-render replaced the node, so fade the fresh one on the next frame
    requestAnimationFrame(() => {
        const fresh = el.board.querySelector(`.sq[data-square="${selected}"] .piece`);
        if (fresh) fresh.classList.add('dragging');
    });
});

el.board.addEventListener('dragover', event => {
    const cell = event.target.closest('.sq');
    if (!cell || !selected || !legalFrom(selected).includes(cell.dataset.square)) return;
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    cell.classList.add('drop-hover');
});

el.board.addEventListener('dragleave', event => {
    const cell = event.target.closest('.sq');
    if (cell) cell.classList.remove('drop-hover');
});

el.board.addEventListener('drop', event => {
    event.preventDefault();
    const cell = event.target.closest('.sq');
    const from = event.dataTransfer.getData('text/plain') || selected;
    if (!cell || !from || !legalFrom(from).includes(cell.dataset.square)) return;
    selected = null;
    commit(from, cell.dataset.square);
});

el.board.addEventListener('dragend', () => {
    el.board.querySelectorAll('.drop-hover').forEach(node => node.classList.remove('drop-hover'));
    render();
});

/* -------------------------------------------------------------- setup dialog */

/** Each segmented control holds the current value in the `on` class of one button. */
function segmentedValue(id) {
    return document.querySelector(`#${id} button.on`).dataset.value;
}

function setSegmented(id, value) {
    document.querySelectorAll(`#${id} button`).forEach(button => {
        button.classList.toggle('on', button.dataset.value === value);
    });
}

function openSetup() {
    setSegmented('opt-opponent', setupChoice.opponent);
    setSegmented('opt-difficulty', setupChoice.difficulty);
    setSegmented('opt-side', setupChoice.side);
    syncSetupFields();
    el.setup.classList.remove('hidden');
}

function syncSetupFields() {
    const againstBot = segmentedValue('opt-opponent') === 'BOT';
    el.fieldDifficulty.classList.toggle('hidden', !againstBot);
    el.fieldSide.classList.toggle('hidden', !againstBot);
    el.difficultyNote.textContent = DIFFICULTY_NOTE[segmentedValue('opt-difficulty')];
}

document.querySelectorAll('.segmented').forEach(group => {
    group.addEventListener('click', event => {
        const button = event.target.closest('button');
        if (!button) return;
        setSegmented(group.id, button.dataset.value);
        syncSetupFields();
    });
});

document.getElementById('setup-start').onclick = () => {
    el.setup.classList.add('hidden');
    newGame({
        opponent: segmentedValue('opt-opponent'),
        difficulty: segmentedValue('opt-difficulty'),
        side: segmentedValue('opt-side')
    }).catch(error => toast(error.message));
};

document.getElementById('setup-cancel').onclick = () => el.setup.classList.add('hidden');

/* ----------------------------------------------------------------- controls */

document.getElementById('btn-new').onclick = () => openSetup();

document.getElementById('btn-undo').onclick = async () => {
    apply(await call(`${API}/${state.gameId}/undo`, { method: 'POST' }), { fresh: true });
};

document.getElementById('btn-flip').onclick = () => {
    flipped = !flipped;
    render();
};

document.getElementById('btn-draw').onclick = async () => {
    apply(await call(`${API}/${state.gameId}/draw`, { method: 'POST' }));
};

document.getElementById('btn-resign').onclick = async () => {
    const name = state.turn === 'WHITE' ? state.whiteName : state.blackName;
    if (!confirm(`${name} resigns?`)) return;
    apply(await call(`${API}/${state.gameId}/resign?colour=${state.turn}`, { method: 'POST' }));
};

document.getElementById('promo-cancel').onclick = () => {
    closePromotion();
    render();
};

document.getElementById('result-new').onclick = () => {
    el.result.classList.add('hidden');
    openSetup();
};

document.getElementById('result-rematch').onclick = () => {
    el.result.classList.add('hidden');
    newGame().catch(error => toast(error.message));
};

document.getElementById('result-close').onclick = () => {
    resultDismissed = true;
    el.result.classList.add('hidden');
};

document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') return;
    if (!el.setup.classList.contains('hidden')) {
        el.setup.classList.add('hidden');
    } else if (!el.promotion.classList.contains('hidden')) {
        closePromotion();
    } else if (!el.result.classList.contains('hidden')) {
        resultDismissed = true;
        el.result.classList.add('hidden');
    } else if (selected) {
        selected = null;
    }
    render();
});

loadOrCreate().catch(error => toast(error.message));
