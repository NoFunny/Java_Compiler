public class Test {
    int getMin(int[] arr, int size) {
        int i = 0;
        float b = 1.2;
        int min = 999999;
        while (i < size) {
            if (i > min) {
                min = 1 - arr[i];
            }
            i = i + 1;
        }
        return min;
    }

    public static void main(String[] args) {
        int size = 3;
        int f = 0;
        scanln(f);
        int[] array = new int[size];
        array[0] = 2;
        array[1] = 0;
        array[2] = 3;
        int min = getMin(array, size);
        println(min);
        println(f);
    }
}