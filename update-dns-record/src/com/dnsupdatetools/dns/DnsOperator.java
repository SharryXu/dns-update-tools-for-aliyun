package com.dnsupdatetools.dns;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse.Record;
import com.aliyuncs.exceptions.ClientException;

import java.util.*;

public class DnsOperator {
    private IAcsClient client = null;

    private static final String regionId = "cn-hangzhou";
    private String accessKeyId = null;
    private String accessSecret = null;

    public DnsOperator(String accessKeyId, String accessSecret) throws IllegalArgumentException {
        if (accessKeyId == null || accessKeyId.isEmpty() ||
                accessSecret == null || accessSecret.isEmpty()) {
            throw new IllegalArgumentException("Please check the access key and access secret.");
        } else {
            this.accessKeyId = accessKeyId;
            this.accessSecret = accessSecret;

            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessSecret);
            this.client = new DefaultAcsClient(profile);
        }
    }

    public boolean AddDomainRecord(String domain, Record record) throws ClientException, IllegalArgumentException {
        if (IsValidateDomain(domain)) {
            AddDomainRecordRequest addRequest = new AddDomainRecordRequest();
            addRequest.setDomainName(domain);
            addRequest.setType(record.getType());
            addRequest.setRR(record.getRR());
            addRequest.setValue(record.getValue());

            AddDomainRecordResponse addResponse = client.getAcsResponse(addRequest);

            return addResponse.getRecordId() != null ? true : false;
        } else {
            throw new IllegalArgumentException("The domain is not valid.");
        }
    }

    public boolean DeleteDomainRecord(String recordId) throws ClientException {
        if (recordId != null && !recordId.isEmpty()) {
            DeleteDomainRecordRequest deleteRequest = new DeleteDomainRecordRequest();
            deleteRequest.setRecordId(recordId);

            DeleteDomainRecordResponse deleteResponse = client.getAcsResponse(deleteRequest);

            return deleteResponse.getRecordId() != null ? true : false;
        } else {
            throw new IllegalArgumentException("The record id cannot be null or whitespace.");
        }
    }

    public boolean UpdateDomainRecords(Record record) throws ClientException {
        if (record != null) {
            UpdateDomainRecordRequest updateRequest = new UpdateDomainRecordRequest();
            updateRequest.setRecordId(record.getRecordId());
            updateRequest.setType(record.getType());
            updateRequest.setRR(record.getRR());
            updateRequest.setValue(record.getValue());

            UpdateDomainRecordResponse updateResponse = client.getAcsResponse(updateRequest);

            return updateResponse.getRecordId() != null ? true : false;
        } else {
            throw new IllegalArgumentException("The record id cannot be null or whitespace.");
        }
    }

    public List<Record> GetDomainRecords(String domain) throws ClientException, IllegalArgumentException {
        if (IsValidateDomain(domain)) {
            DescribeDomainRecordsRequest request = new DescribeDomainRecordsRequest();
            request.setDomainName(domain);

            return client.getAcsResponse(request).getDomainRecords();
        } else {
            throw new IllegalArgumentException("The domain name is not valid.");
        }
    }

    public List<Record> GetSpecificRecords(String domain, String hostRecord, String type) throws ClientException {
        List<Record> records = GetDomainRecords(domain);
        List<Record> matchRecords = new ArrayList<Record>();

        for (Record record : records) {
            if (record.getRR().equalsIgnoreCase(hostRecord) && record.getType().equalsIgnoreCase(type)) {
                matchRecords.add(record);
            }
        }

        return matchRecords;
    }

    public static boolean IsValidateDomain(String domain) {
        String domainNameInUpper = domain.toUpperCase();

        for (String domainSuffix : DomainConstants.DomainSuffixs) {
            if (domainNameInUpper.endsWith(domainSuffix)) {
                return true;
            }
        }

        return false;
    }

    public static void PrintDomainRecords(String title, List<Record> records) {
        System.out.println(String.format("%1$s", title));

        for (Record record : records) {
            System.out.println(String.format("Record Type: %1$6s , Host Record: %2$5s , Record Value: %3$20s, Status: %4$6s.",
                    record.getType(), record.getRR(), record.getValue(), record.getStatus()));
        }
    }

    public void PrintRecords(String domain) {
        try {
            List<Record> latestRecords = this.GetDomainRecords(domain);

            DnsOperator.PrintDomainRecords("Latest DNS records: ", latestRecords);
        } catch (Exception e) {
            System.out.println("Cannot print because: " + e.getMessage());
        }
    }
}
