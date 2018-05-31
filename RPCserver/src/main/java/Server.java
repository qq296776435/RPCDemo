public class Server {
    public static void main(String[] args) throws Exception {
        Hello hello = new HelloImpl();
        RPCFramework.export(hello,hello.getClass(),8888);
    }
}
