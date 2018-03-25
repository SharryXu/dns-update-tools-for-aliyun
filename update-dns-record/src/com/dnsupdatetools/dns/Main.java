package com.dnsupdatetools.dns;

import com.dnsupdatetools.ip.*;

import java.util.*;

import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse.Record;
import com.sun.jndi.dns.DnsContext;

public class Main {
    public static String manual = "Update aliyun's dns record." +
            "\n Usage: java -jar update-dns-record.jar [Options] -n <accesskey-id> -p <access-secret> -d <domain> -r <record>" +
            "\n\n[Options]:" +
            "\n-h  view help file.";

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

            boolean anyRecordChanged = false;

            try {
                List<Record> records = dnsOperator.GetSpecificRecords(domain, hostRecord, recordType);

                String currentIpAddress = ipAddressOperator.GetIpAddressWithRetry();

                for (Record record : records) {
                    String originalIpAddress = record.getValue();
                    record.setValue(currentIpAddress);

                    if (record.getValue() == currentIpAddress) {
                        System.out.println("\nNo need change.");
                    } else {
                        if (dnsOperator.UpdateDomainRecords(record)) {
                            System.out.println(String.format("\nSuccessfully updated the record from %1$s to %2$s.",
                                    originalIpAddress, currentIpAddress));

                            anyRecordChanged = true;
                        } else {
                            System.out.println("\nFailed to updated the record.");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(String.format("\n%1$s", e.getMessage()));
            } finally {
                if (anyRecordChanged) {
                    dnsOperator.PrintRecords(domain);

                    System.exit(0);
                } else {
                    System.exit(1);
                }
            }
        } else {
            System.out.println(manual);

            System.exit(1);
        }
    }

    private static boolean PreProcess(String[] args) {
        String accessKeyId = "";
        String accessSecret = "";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                    if (i + 1 < args.length) {
                        accessKeyId = args[i + 1];
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        accessSecret = args[i + 1];
                    }
                    break;
                case "-d":
                    if (i + 1 < args.length) {
                        domain = args[i + 1];
                    }
                    break;
                case "-r":
                    if (i + 1 < args.length) {
                        hostRecord = args[i + 1];
                    }
                    break;
                case "-h":
                case "--help":
                    System.out.println(manual);

                    System.exit(1);
                    break;
                default:
                    break;
            }
        }

        if (accessKeyId == null || accessKeyId.isEmpty()) {
            System.out.println("Please provide access-key-id.");

            System.exit(1);

            return false;
        }

        if (accessSecret == null || accessSecret.isEmpty()) {
            System.out.println("Please provide access-secret.");

            System.exit(1);

            return false;
        }

        if (hostRecord == null || hostRecord.isEmpty()) {
            System.out.println("Please set appropriate host record.");

            System.exit(1);

            return false;
        }

        if (domain == null || ! DnsOperator.IsValidateDomain(domain)) {
            System.out.println("Please set appropriate domain.");

            System.exit(1);

            return false;
        }

        try {
            dnsOperator = new DnsOperator(accessKeyId, accessSecret);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());

            return false;
        }

        return true;
    }
}
