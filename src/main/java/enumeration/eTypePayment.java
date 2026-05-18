package enumeration;
public enum eTypePayment{
       SUBSCRIPTION, GREENFEE, COTISATION, LESSON;
       
       public static String SUBSCRIPTION() {  // Enumeration is a type of a class. We can define our own methods.
        return eTypePayment.SUBSCRIPTION.toString();
       }
       public static String GREENFEE() {  // Enumeration is a type of a class. We can define our own methods.
        return eTypePayment.GREENFEE.toString();
       }
       public static String COTISATION() {  // Enumeration is a type of a class. We can define our own methods.
        return eTypePayment.COTISATION.toString();
       }
       public static String LESSON() {  // Enumeration is a type of a class. We can define our own methods.
        return eTypePayment.LESSON.toString();
       }
   } // end 