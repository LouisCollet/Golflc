
package entite;

import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;

public class Report {
    private String reportKey;
        private String studentNumber;
        private String school;
        private Double points;

        public Report(String reportKey, String studentNumber, String school, Double points) {
            this.reportKey = reportKey;
            this.studentNumber = studentNumber;
            this.school = school;
            this.points = points;
        }
public Report(){
    
}
        public String getReportKey() {
            return reportKey;
        }

        public void setReportKey(String reportKey) {
            this.reportKey = reportKey;
        }

        public String getStudentNumber() {
            return studentNumber;
        }

        public void setStudentNumber(String studentNumber) {
            this.studentNumber = studentNumber;
        }

        public String getSchool() {
            return school;
        }

        public void setSchool(String school) {
            this.school = school;
        }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

        @Override
        public String toString() {
            return NEW_LINE +
                   "  reportKey=" + reportKey + TAB +
                   ", studentNumber=" + studentNumber +TAB +
                   ", school=" + school + TAB +
                   ", points=" + points
                   ;
        }
    } // end class
