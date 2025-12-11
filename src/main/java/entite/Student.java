/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entite;
// Java program to demonstrate working of Comparator 
// interface and Collections.sort() to sort according 
// to user defined criteria. 

public class Student { 
    public int rollno; 
    public double scoreDifferential; 
    String name, address; 
  
    // Constructor 
    public Student(int rollno, double scoreDifferential, String name,  String address) { 
        this.rollno = rollno; 
        this.scoreDifferential = scoreDifferential; 
        this.name = name; 
        this.address = address; 
    } 
  
    // Used to print student details in main() 
    public String toString() { 
        return this.rollno + " " + this.scoreDifferential + " " + this.name +  " " + this.address; 
    } 
/*    
 public class Sortbyroll implements Comparator<Student> { 
    // Used for sorting in ascending order of roll number 
    public int compare(Student a, Student b) { 
        return a.rollno - b.rollno; 
    } 
    
    
} // end class
*/
} // end class