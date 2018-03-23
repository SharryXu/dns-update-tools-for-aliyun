package com.dnsupdatetools.dns;

import com.dnsupdatetools.ip.*;

import java.util.*;

import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse.Record;

public class Main {
    public static String manual = "Update aliyun's dns record." +
            "\n Usage: java -jar update-dns-record.jar [Options] -n <accesskey-id> -p <access-secret>" +
            "\n\n[Options]:" +
            "\n-h  view help file." +
            "\n-d  set domain name." +
            "\n-r  set host record.";

    private static String domain;
    private static String hostRecord;
    // Right now, we only use ip address to assign the value and
    // if the value of record type is't the A, it will be meaning less.
    private static final String recordType = "A";
    private static final String defaultTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private static IpAddressOperator ipAddressOperator = null;
    private static DnsOperator dnsOperator = null;

    static {
        ipAddressOperator = new IpAddressOperator();
    }

    public static void main(String[] args) {
        boolean needContinueProcess = PreProcess(args);

        if (needContinueProcess) {
            dnsOperator.PrintRecords(domain);

            try {
                List<Record> records = dnsOperator.GetSpecificRecords(domain, hostRecord, recordType);

                String currentIpAddress = ipAddressOperator.GetIpAddressWithRetry();
                String originalIpAddress = null;

                for (Record record : records) {
                    originalIpAddress = record.getValue();
                    record.setValue(currentIpAddress);

                    if (dnsOperator.UpdateDomainRecords(record.getRecordId(), record)) {
                        System.out.println(String.format("\nSuccessfully updated the record from %1$s to %2$s.",
                                originalIpAddress, currentIpAddress));
                    } else {
                        System.out.println("\nFailed to updated the record.");
                    }
                }
            } catch (Exception e) {
                System.out.println(String.format("\n%1$s", e.getMessage()));
            } finally {
                dnsOperator.PrintRecords(domain);

                System.out.println("===================================================");
            }
        }
    }

    private static boolean PreProcess(String[] args) {
        String accessKeyId = "";
        String accessSecret = "";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                    if (i + 1 < args.length && !args[i + 1].isEmpty()) {
                        accessKeyId = args[i + 1];
                    } else {
                        System.out.println("Please provide access-key-id.");

                        System.exit(1);

                        return false;
                    }
                    accessKeyId = args[i];
                    break;
                case "-p":
                    if (i + 1 < args.length && !args[i + 1].isEmpty()) {
                        accessKeyId = args[i + 1];
                    } else {
                        System.out.println("Please provide access-secret.");

                        System.exit(1);

                        return false;
                    }
                    accessSecret = args[i];
                    break;
                case "-h":
                case "-help":
                case "--help":
                    System.out.println(manual);
                    break;
                case "-d":
                    if (i + 1 < args.length && DnsOperator.IsValidateDomain(args[i + 1])) {
                        domain = args[i + 1];
                    } else {
                        System.out.println("Please set appropriate domain.");

                        System.exit(1);

                        return false;
                    }
                    break;
                case "-r":
                    if (i + 1 < args.length && !args[i + 1].isEmpty()) {
                        hostRecord = args[i + 1];
                    } else {
                        System.out.println("Please set appropriate host record.");

                        System.exit(1);

                        return false;
                    }
                    break;
                default:
                    break;
            }
        }

        dnsOperator = new DnsOperator(accessKeyId, accessSecret);

        return true;
    }
}
