package org.example.factoryPattern;

public class AbstractFactoryPattern {
    // Factory of Factory is called Abstract Factory Pattern

    // Family of related Objects that can we use together without spefic the concrete class

    interface Button{
        void render();
    }

    interface Chekbox{
        void render();
    }

    static class WindowButton implements Button{
        @Override
        public void render() {
            System.out.println("WindowButton");
        }
    }

    static class WindowCheckBox implements Chekbox{
        @Override
        public void render() {
            System.out.println("WindowCheckBox");
        }
    }

    static class MacButton implements Button{
        @Override
        public void render() {
            System.out.println("MacButton");
        }
    }

    static class MacCheckBox implements Chekbox{
        @Override
        public void render() {
            System.out.println("MacCheckBox");
        }
    }

    interface GUIFactory{
        Button createButton();
        Chekbox createChekbox();
    }

    static class WindowGUI implements GUIFactory{
        @Override
        public Button createButton() {return new WindowButton();}

        @Override
        public Chekbox createChekbox() {
            return new WindowCheckBox();
        }
    }


    static class MacGUI implements GUIFactory{
        @Override
        public Button createButton() {return new MacButton();}

        @Override
        public Chekbox createChekbox() {
            return new MacCheckBox();
        }
    }

    static class Application{
        private final Button button;
        private final Chekbox chekbox;
        Application(GUIFactory guiFactory){
            this.button = guiFactory.createButton();
            this.chekbox = guiFactory.createChekbox();
        }
        void renderUi(){
            button.render();
            chekbox.render();
        }
    }

    public static void main(String[] args) {
        GUIFactory gui = new MacGUI();
        new Application(gui).renderUi();  // if we want to switch to windowGui need to make only one change :- new MaxGUI() -> new WindowGUI();
    }

}
