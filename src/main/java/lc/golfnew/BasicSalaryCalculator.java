/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

/**
 *
 * @author collet
 */
public class BasicSalaryCalculator {
  private double basicSalary;
 
  public double getBasicSalary() {
    return basicSalary;
  }
 
  public void setBasicSalary(double basicSalary) {
    if (basicSalary < 0) {
      throw new IllegalArgumentException("Negative salary is invalid.");
    }
    this.basicSalary = basicSalary;
  }
 
  public double getGrossSalary() {
    return this.basicSalary + getSocialInsurance() + getAdditionalBonus();
  }
 
  public double getSocialInsurance() {
    return this.basicSalary * 25 / 100;
  }
 
  public double getAdditionalBonus() {
    return this.basicSalary / 10;
  }
}
