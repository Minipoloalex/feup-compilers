/*
Tests:
   -class extensions
*/
import classA;

class test2 extends classA {

    public int f1(int a, int b) {
        a = a + b;
        return a;
    }

    public test2 f2() {
        return new test2();
    }

    public int f3(classA p) {
        return 3;
    }

    public int f4() {
        test2 p;
        p = this.f2();
        this.f3(new classA());
        this.f3(new test2());
        this.f3(p);
        return 4;
    }

    public int foo() {
        test2 objA;
        classA objA2;
        test2 objt2;

        objA = new test2();
        objA2 = new classA();
        objt2 = new test2();

        objA.func2(4);
        objA2.func2(4);
        this.func2(3);
        objt2.func2(3);

        objt2.f1(2, 3);

        this.getClassA();
        this.f4();
        return 5;
    }

    public static void main(String[] args) {
        test2 x;
        x = new test2();
        x.foo();
    }

    public boolean getClassA() {
        return false;
    }
}