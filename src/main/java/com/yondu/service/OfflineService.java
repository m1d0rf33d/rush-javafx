package com.yondu.service;

import com.yondu.App;
import com.yondu.model.OfflineTransaction;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.GIVE_POINTS_ENDPOINT;
import static com.yondu.AppContextHolder.MEMBER_LOGIN_ENDPOINT;
import static com.yondu.model.constants.AppConfigConstants.DIVIDER;
import static com.yondu.model.constants.AppConfigConstants.RUSH_HOME;

/**
 * Created by lynx on 2/22/17.
 */
public class OfflineService {

    private ApiService apiService = new ApiService();

    public void givePoints() {
        File file = new File(RUSH_HOME + DIVIDER + AppConfigConstants.OFFLINE_TRANSACTION_FILE);

        if (file.exists()) {
            //Read file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                List<String> transactions = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    String mobileNumber = arr[0].split("=")[1];
                    String totalAmount = arr[1].split("=")[1];
                    String orNumber = arr[2].split("=")[1];
                    String date = arr[3].split("=")[1];
                    String status = arr[4].split("=")[1];
                    String message = arr[5].split("=")[1];

                    OfflineTransaction offlineTransaction = new OfflineTransaction();
                    offlineTransaction.setAmount(totalAmount);
                    offlineTransaction.setMobileNumber(mobileNumber);
                    offlineTransaction.setOrNumber(orNumber);
                    offlineTransaction.setDate(date);
                    offlineTransaction.setStatus(status);
                    offlineTransaction.setMessage(message);

                    if (status.equalsIgnoreCase("Pending")) {
                        offlineTransaction = sendPoints(offlineTransaction);
                    }


                    String l = "mobileNumber=" + offlineTransaction.getMobileNumber()+
                            ":totalAmount=" + offlineTransaction.getAmount() +
                            ":orNumber=" + offlineTransaction.getOrNumber() +
                            ":date=" + offlineTransaction.getDate() +
                            ":status=" + offlineTransaction.getStatus() +
                            ":message=" + offlineTransaction.getMessage();

                    transactions.add(l);

                }

                PrintWriter writer = new PrintWriter(file);
                writer.print("");
                writer.close();

                PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
                for (String trans : transactions) {
                    byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(trans.getBytes());
                    fstream.println(new String(encodedBytes));
                }

                fstream.flush();
                fstream.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public OfflineTransaction sendPoints(OfflineTransaction offlineTransaction) {

        SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
        String date = df.format(new Date());
        offlineTransaction.setDate(date);

        offlineTransaction.setMessage("");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, offlineTransaction.getMobileNumber()));

        String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, offlineTransaction.getOrNumber()));
                params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, offlineTransaction.getAmount().replace(",", "")));
                url = BASE_URL + GIVE_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid", (String) data.get("id"));
                url = url.replace(":employee_id",  App.appContextHolder.getEmployeeId());
                JSONObject json = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                if (json != null) {
                    if (!json.get("error_code").equals("0x0")) {
                        JSONObject error = (JSONObject) json.get("errors");
                        String errorMessage = "";
                        if (error != null) {
                            if (error.get("or_no") != null) {
                                List<String> l = (ArrayList<String>) error.get("or_no");
                                errorMessage = l.get(0);
                            }
                            if (error.get("amount") != null) {
                                List<String> l = (ArrayList<String>) error.get("amount");
                                errorMessage = l.get(0);
                            }
                        }
                        if (json.get("message") != null) {
                            errorMessage = (String) json.get("message");
                        }
                        offlineTransaction.setMessage(errorMessage);
                        offlineTransaction.setStatus("Failed");
                    } else {
                        offlineTransaction.setStatus("Submitted");
                        offlineTransaction.setMessage("Points earned");
                    }
                } else {
                    offlineTransaction.setMessage((String) jsonObject.get("Network error"));
                    offlineTransaction.setStatus("Pending");
                }
            } else {
                offlineTransaction.setMessage((String) jsonObject.get("message"));
                offlineTransaction.setStatus("Failed");
            }
        }
        return offlineTransaction;
    }
}
