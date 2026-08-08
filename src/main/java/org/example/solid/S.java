package org.example.solid;

public class S {
    // Single Responsblity principal

    // A class should have only one region to change

    // bad design
    class Employee{
        void salaryCalculator(){}
        void saveToDatabase(){}
        void generatePaySlipPdf(){}
    }

    // Good design
     class SalaryCalculator{
        void calculateSalary(){}
    }
    class SaveToDatabase{
        void saveToDatabase(){}
    }
    class GeneratePaySlipPdf{
        void generatePaySlipPdf(){}
    }


}
