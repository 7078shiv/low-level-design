package org.example.solid;

public class I {
    // Interface segregation principal
    // Don't force a class to implement that methods that it doesn't need

    // bad code

    interface Worker{
        void work();
        void eat();
    }

    class RobotWroker implements Worker{
        @Override
        public void work() {
            System.out.println("working");
        }

        @Override
        public void eat() {
            throw new UnsupportedOperationException();
        }
    }

    // Good code

    interface EatableWorker{
        void eat();
    }

    interface WorkAbleWorker{
        void work();
    }

    class HumanWroker implements EatableWorker,WorkAbleWorker{
        @Override
        public void work() {
            System.out.println("working");
        }

        @Override
        public void eat() {
            System.out.println("eating");
        }
    }

    class Robot implements WorkAbleWorker{
        @Override
        public void work(){
            System.out.println("working");
        }
    }
}
