import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

public class RPCFramework {
    public static void export(Object service,Class interfaceClazz,int port) throws Exception {
        System.out.println(service.getClass().getName()+"on port:"+port);
        ServerSocket serverSocket = new ServerSocket(port);
        for (;;){
            final Socket socket=serverSocket.accept();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        try {
//                            String interfaceName = inputStream.readUTF();
                            String methodName = inputStream.readUTF();
                            Class<?>[] parameterTypes = (Class<?>[]) inputStream.readObject();
                            Object[] args = (Object[]) inputStream.readObject();
                            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                            try {
//                                if (!interfaceName.equals(interfaceClazz.getName())){//service.getClass().getName()?
//                                    throw new IllegalAccessException("Interface name not match. Refer: "+
//                                            interfaceName+"Export: "+interfaceClazz.getName());
//                                }
                                Method method = service.getClass().getMethod(methodName,parameterTypes);
                                Object result = method.invoke(service,args);
                                outputStream.writeObject(result);
                            }catch (Throwable t){
                                outputStream.writeObject(t);
                            }finally {
                                outputStream.close();
                            }
                        }finally{
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
    }
    public static <T> T refer(Class<T> interfaceClazz,String host,int port){
        System.out.println(interfaceClazz.getName()+"from "+host+": "+port);
        return (T) Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class<?>[]{interfaceClazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = new Socket(host,port);
                        try {
                            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                            try {
                                //interfaceName no need?
                                outputStream.writeUTF(method.getName());
                                outputStream.writeObject(method.getParameterTypes());
                                outputStream.writeObject(args);
                                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                                try {
                                    Object result = inputStream.readObject();
                                    if (result instanceof Throwable){
                                        throw (Throwable) result;
                                    }
                                    return result;
                                }finally {
                                    inputStream.close();
                                }
                            }finally {
                                outputStream.close();
                            }
                        }finally {
                            socket.close();
                        }
                    }
                }
        );
    }

}
