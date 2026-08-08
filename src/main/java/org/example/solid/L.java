package org.example.solid;

public class L {
    // LisKov Substitution Principal

    // it states that A subclass should be able to replace its parent class without breaking the program

    // bad code

    static class Bird{
        public void fly(){
            System.out.println("bird is flying");
        }
    }

    static class Ostrich extends Bird{
        public void fly(){
            throw new UnsupportedOperationException("ostrish Cant fly");
        }
    }

    // Good code

    interface FlyingBird{
        void fly();
    }
    interface NormalBird{
    }

    static class Sparrow implements FlyingBird{
        @Override
        public void fly() {
            System.out.println("sparrow is flying");
        }
    }

    static class Ostrich2 implements NormalBird{
    }

    public static void main(String[] args) {
        // bad
        // Bird bird = new Ostrich();
        //bird.fly(); // this will throw exception

        Bird bird2 = new Bird();
        bird2.fly();

        // Good
        FlyingBird bird = new Sparrow();
        NormalBird nBird = new Ostrich2();
        bird.fly();
    }


}
