package org.example.solid;

public class D {
    // Dependency Inversion Principal

    // Depend on abstraction not on concrete classes

    // Bad Code

    class MysqlDatabase{
        void save(){

        }
    }

    class UserService{
        MysqlDatabase mysqlDatabase = new MysqlDatabase();  // directly depend on concrete class
        // now suppose if i want to save in mongoDb Database so when we need to change code
        public void saveToDb(){
            mysqlDatabase.save();
        }
    }

    // Good Code

    interface Database{
        void save();
    }

    class MysqlDatabase1 implements Database{

        @Override
        public void save() {
            System.out.println("Save in mysql database");
        }
    }

    class MongoDbDatabase implements Database{
        @Override
        public void save() {
            System.out.println("Save in mongo database");
        }
    }

    class UserServiceGood{
       private final Database db;
       UserServiceGood(Database db){
           this.db = db;
       }
       public void save(){
           db.save();
       }
    }

}
