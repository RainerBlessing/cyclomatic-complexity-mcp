public class SimpleJava {
    public void simpleMethod() {
        System.out.println("Hello");
    }

    public int complexMethod(int x) {
        if (x > 0) {
            for (int i = 0; i < x; i++) {
                if (i % 2 == 0) {
                    System.out.println(i);
                }
            }
        } else if (x < 0) {
            while (x < 0) {
                x++;
            }
        }
        return x;
    }
}
