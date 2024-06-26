import io;

class MyClass {

    public int add(int a, int b){
        int c;
        c = a + b;
        return c;
    }

    public int inc(int a){
        int b;
        b = a + 1;
        return b;
    }

    public int half(int a){
        int b;
        b = a / 2;
        return b;
    }

    public static void main(String[] args){
        int a;
        int b;
        int c;
        MyClass d;

        d = new MyClass();

        a = 10;
        b = 20;
        c = d.add(a, b);
        io.println(c); // 30

        a = 10;
        b = d.inc(a);
        c = d.add(a, b);
        a = d.half(c);
        b = d.inc(a);
        c = d.add(a, b);
        a = d.half(c);
        io.println(a); // 10

        a = 10;
        b = d.inc(a);
        c = d.add(a, b);
        a = d.half(c);
        b = d.inc(a);
        c = d.add(a, b);
        a = d.half(c);
        b = d.inc(a);
        c = d.add(a, b);
        io.println(c); // 21

        a = 10;
        b = 1000;
        c = d.inc(d.inc(d.inc(d.inc(d.inc(a)))));
        c = d.add(c, d.half(d.half(d.half(b))));
        io.println(d.half(c)); // 72


    }

}