package utils;

import java.util.Comparator;

//@SuppressWarnings("rawtypes")
public class ColumnComparator implements Comparator
{
int columnToSort;

public ColumnComparator(int columnToSort)
{
this.columnToSort = columnToSort;
}  //end constructor

//Compare method
    @Override
public int compare(Object o1, Object o2)
    {
    int[] row1 = (int[]) o1;
    int[] row2 = (int[]) o2;

    int intRow1 = (row1[columnToSort]);
    int intRow2 = (row2[columnToSort]);

 //   return new Integer(intRow1).compareTo(new Integer(intRow2));Integer integer = Integer.valueOf(i);
    return Integer.valueOf(intRow1).compareTo(Integer.valueOf(intRow2));
} //end method
} //end class