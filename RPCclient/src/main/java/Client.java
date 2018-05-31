public class Client {
    public static void main(String[] args) {
        Hello hello = (Hello) RPCFramework.refer(Hello.class,"localhost",8888);
        System.out.println(hello.sayHello("hello, u've got HelloImpl!"));
        System.out.println(hello.sayGoobye("88"));
    }
}
