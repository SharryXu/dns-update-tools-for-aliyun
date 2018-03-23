package com.dnsupdatetools.ip;

public class Main {
    private static int RetryTimes;
    private static boolean needExecute = true;

    public static void main(String[] args) throws Exception {
        PreProcess(args);

        if (needExecute) {
            IpAddressOperator ipAddressOperator = new IpAddressOperator();

            String ipAddress = ipAddressOperator.GetIpAddressWithRetry();

            if (ipAddress != null) {
                System.out.println(ipAddress);
            } else {
                throw new Exception("Cannot get appropriate ip address.");
            }
        }
    }

    private static void PreProcess(String[] args) {
        //Check help parameter
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                case "-help":
                case "--help":
                    needExecute = false;

                    System.out.println("Get current machine's pulic IP address." +
                                    "\nUsage: java -jar AliDnsGet.jar [Options]" +
                                    "\n\n[Options]:" +
                                    "\n-h  view help file." +
                                    "\n-r  set retry times.");
                    break;
                case "-r":
                    if (i + 1 < args.length) {
                        RetryTimes = Integer.parseInt(args[i + 1]);
                    } else {
                        System.out.println("Please set appropriate parameter for the retry time.");

                        needExecute = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
