

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class DurakRating {
    private int roundCount = 0;
    private boolean isKickedOut = false;
    private String tableNum = "1";

    private static int port[] = {10771, 10772, 10773, 10774, 10775};
    private static boolean sequence[] = {true, false, true, false, true};

    static String loseToken = "$2a$"; //токен который проиграет
    static String winToken = "$2a$";  // токен который выиграет


    private static int betAmount = 100;
    private static int tableCount = 5;
    static AtomicInteger gameCount = new AtomicInteger(0);
    private static int gameLimit = 700000;

    public static void main(String[] args) throws Exception {

        for (int i = 0; i < 2; i++) {
            final int finalI = i;
            Thread winner = new Thread(() -> {
                DurakRating client = new DurakRating();
                try {
                    client.connectWinner(port[finalI], winToken, true, loseToken, finalI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            winner.start();
            Thread.sleep(3000);

        }

        for (int i = 0; i < 2; i++) {
            final int finalI = i;
            Thread winner = new Thread(() -> {
                DurakRating client = new DurakRating();
                try {
                    client.connectWinner(port[finalI + 2], loseToken, false, winToken, finalI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            winner.start();
            Thread.sleep(3000);

        }


    }

    void connectWinner(int port, String token, boolean isWinner, String loseToken, int position) throws Exception {
        boolean start = false;

        String hostname = "65.21.92.165";

        Socket clientSocket = null;
        DataOutputStream os = null;
        BufferedReader is = null;


        try {
            clientSocket = new Socket(hostname, port);
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostname);
            Thread.sleep(5000);
            //  connectWinner(port, winToken,true,loseToken);
        }

        if (clientSocket == null || os == null || is == null) {
            System.err.println("Something is wrong. One variable is null.");
            return;
        }
        String mSighn_key = "c{\"p\":10,\"d\":\"Xiaomi raphael\",\"v\":\"1.8.1\",\"tz\":\"+03:00\",\"pl\":\"android\",\"l\":\"ru\",\"n\":\"durak.android\"}\n";

        String dur3 = "auth{\"token\":\"" + token + "\"}\n";
        String dur4 = "create{\"bet\":" + betAmount + ",\"password\":\"0917\",\"fast\":true,\"sw\":false,\"nb\":true,\"ch\":true,\"players\":2,\"deck\":24}\n";
        String ready = "ready\n";
        String take = "take\n";
        String pass = "pass\n";
        String send2 = "";
        String send44 = "";
        String done = "done\n";
        String sur = "surrender\n";
        int counter = 0;

        os.write(mSighn_key.getBytes(StandardCharsets.UTF_8));
        Thread.sleep(500);

        os.write(dur3.getBytes(StandardCharsets.UTF_8));
        Thread.sleep(500);

        os.write(dur4.getBytes(StandardCharsets.UTF_8));
        Thread.sleep(500);


        try {
            while (!isKickedOut) {
                String responseLine = is.readLine();
                System.out.println(responseLine);
                if (responseLine.contains("game{\"id\"") && !start) {
                    start = true;
                    tableNum = (String) responseLine.substring(10, 23);
                    System.out.println("Table: " + tableNum);

                    Thread loser = new Thread(() -> {
                        LoserFinal1 loser1 = new DurakRating.LoserFinal1();
                        try {
                            loser1.connectLooser(tableNum, port, loseToken, isWinner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    loser.start();
                }

                int emptyCounter = 0;
                while (start && !isKickedOut) {
                    String responseLine1 = is.readLine();
                    if (responseLine1.contains("mode{\"0\":4,\"1\":8}") || responseLine1.contains("mode{\"0\":8,\"1\":4}")) {
                        counter++;
                    }
                    if (responseLine1.equals("")) {
                        emptyCounter++;
                        if (emptyCounter >= 2) {
                            isKickedOut = true;
                        }
                    } else {
                        emptyCounter = 0;
                    }


                    if (gameCount.get() >= gameLimit) {

                        System.exit(0);

                    }

                    if (responseLine1.contains("btn_ready_on")) {
                        gameCount.incrementAndGet();
                        Thread.sleep(30);
                        os.write(ready.getBytes(StandardCharsets.UTF_8));
                    }
                    if (responseLine1.contains("p_on{\"id\":1}")) {
                        os.write(ready.getBytes(StandardCharsets.UTF_8));
                    }
                    if (responseLine1.contains("hand{\"cards\":[\"")) {
                        send2 = responseLine1.substring(15, 17);
                        if (send2.contains("1")) {
                            send2 = send2 + "0";
                        }
                    }


                    if (!isWinner && roundCount >= 4) {

                        os.write(sur.getBytes(StandardCharsets.UTF_8));
                        System.out.println(Thread.currentThread().getName() + "::" + gameCount.get());
                        roundCount = 0;

                    }

                    if (responseLine1.contains("mode{\"0\":1,\"1\":8}")) {
                        String send11 = "t{\"c\":\"" + send2 + "\"}\n";
                        os.write(send11.getBytes(StandardCharsets.UTF_8));

                    }
                    if (responseLine1.contains("p{\"id\":1,\"user\":null}")) {
                        isKickedOut = true;
                        String str4 = "leave{\"id\":" + tableNum + "}\n";
                        os.write(str4.getBytes(StandardCharsets.UTF_8));
                        os.write(str4.getBytes(StandardCharsets.UTF_8));
                    }
                    if (responseLine1.contains("t{\"id\":1,\"c\":\"")) {
                        send44 = responseLine1.substring(14, 16);
                        if (send44.contains("1")) {
                            send44 = send44 + "0";
                        }

                        String take1 = "take\n";
                        os.write(take1.getBytes(StandardCharsets.UTF_8));
                    }


                    if (responseLine1.contains("mode{\"0\":0,")) {
                        String passing = "pass\n";
                        os.write(passing.getBytes(StandardCharsets.UTF_8));
                        roundCount++;
                    }
                    if (responseLine1.contains("p{\"id\":1,\"user\":null}") || isKickedOut) {
                        isKickedOut = true;
                        String str4 = "leave{\"id\":" + tableNum + "}\n";
                        os.write(str4.getBytes(StandardCharsets.UTF_8));
                        if (position == 1 || position == 2) {

                            os.close();
                            is.close();
                            clientSocket.close();
                            System.out.println("making new game");
                            Thread.sleep(5000);


                        }
                    }
                    if (responseLine1.contains("alert{") || responseLine1.contains("err{")) {
                        isKickedOut = true;
                    }
                }

                if (responseLine == "123456789") {
                    break;
                }
            }
            os.close();
            is.close();
            clientSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Trying to connect to unknown host: " + e);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        } catch (NullPointerException e) {
            System.err.println("null:  " + e);
        } finally {
        }
    }


    public class LoserFinal1 {
        String tablename = "1";
        int counter = 0;

        public void connectLooser(String tablename2, int port, String token, boolean isWinner) throws Exception {

            String hostname = "65.21.92.165";
            Socket clientSocket = null;
            DataOutputStream os = null;
            BufferedReader is = null;
            try {
                clientSocket = new Socket(hostname, port);
                os = new DataOutputStream(clientSocket.getOutputStream());
                is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: " + hostname);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: " + hostname);
            }
            if (clientSocket == null || os == null || is == null) {
                System.err.println("Something is wrong. One variable is null.");
                return;
            }
            String dur1 = "c{\"p\":10,\"d\":\"Xiaomi raphael\",\"v\":\"1.8.1\",\"tz\":\"+03:00\",\"pl\":\"android\",\"l\":\"ru\",\"n\":\"durak.android\"}\n";
            String dur3 = "auth{\"token\":\"" + token + "\"}\n";
            String ready = "ready\n";
            String done = "done\n";
            String sur = "surrender\n";
            String send2 = "";
            String send44;

            os.write(dur1.getBytes(StandardCharsets.UTF_8));
            Thread.sleep(500);



            os.write(dur3.getBytes(StandardCharsets.UTF_8));
            Thread.sleep(500);


            Thread.sleep(1000);
            String join = "join{\"password\":\"0917\",\"id\":" + tablename2 + "}\n";
            System.out.println("L:" + tablename2);
            os.write(join.getBytes(StandardCharsets.UTF_8));
            Thread.sleep(100);
            os.write(ready.getBytes(StandardCharsets.UTF_8));

            int emptyCounter = 0;
            try {

                while (!isKickedOut) {

                    String responseLine1 = is.readLine();
                    //      System.out.println(responseLine1);


                    if (responseLine1.equals("")) {
                        emptyCounter++;
                        if (emptyCounter >= 2) {
                            isKickedOut = true;
                        }
                    } else {
                        emptyCounter = 0;
                    }


                    if (responseLine1.contains("btn_ready_on")) {
                        Thread.sleep(20);
                        os.write(ready.getBytes(StandardCharsets.UTF_8));
                    }
                    if (responseLine1.contains("hand{\"cards\":[\"")) {
                        send2 = responseLine1.substring(15, 17);
                        if (send2.contains("1")) {
                            send2 = send2 + "0";
                        }
                    }
                    if (isWinner && roundCount >= 4) {

                        os.write(sur.getBytes(StandardCharsets.UTF_8));
                        System.out.println(Thread.currentThread().getName() + "::" + gameCount.get());
                        roundCount = 0;

                    }

                    if (responseLine1.contains("mode{\"0\":8,\"1\":1}")) {
                        String send11 = "t{\"c\":\"" + send2 + "\"}\n";
                        os.write(send11.getBytes(StandardCharsets.UTF_8));

                    }

                    if (responseLine1.contains("p{\"id\":0,\"user\":null}")) {
                        isKickedOut = true;
                        String str4 = "leave{\"id\":" + tablename + "}\n";
                        os.write(str4.getBytes(StandardCharsets.UTF_8));
                        Thread.sleep(100);
                        os.close();
                        is.close();
                        clientSocket.close();
                    }
                    if (responseLine1.contains("t{\"id\":0,\"c\":\"")) {
                        send44 = responseLine1.substring(14, 16);
                        if (send44.contains("1")) {
                            send44 = send44 + "0";
                        }

                        String take1 = "take\n";
                        os.write(take1.getBytes(StandardCharsets.UTF_8));
                        continue;
                    }
                    if (responseLine1.contains("\"1\":0")) {
                        String passing = "pass\n";
                        os.write(passing.getBytes(StandardCharsets.UTF_8));
                        roundCount++;
                    }

                    if (isKickedOut) {
                        os.close();
                        is.close();
                        clientSocket.close();

                    }
                    if (responseLine1.contains("alert{") || responseLine1.contains("err{")) {
                        isKickedOut = true;
                    }

                    if (responseLine1.contains("b{\"id\":0,\"c\":\"")) {
                        os.write(done.getBytes(StandardCharsets.UTF_8));
                        roundCount++;
                    }

                    if (responseLine1 == "12345689") {
                        break;
                    }
                }

                os.close();
                is.close();
                clientSocket.close();
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }


}
