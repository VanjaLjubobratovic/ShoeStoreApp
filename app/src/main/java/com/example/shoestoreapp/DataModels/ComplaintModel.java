package com.example.shoestoreapp.DataModels;

public class ComplaintModel {
    private String email;
    private String complaintType;
    private String orderCode;
    private String model;
    private boolean resend;
    private String complaint;
    private String resolved;
    private Integer size;
    private String complaintId;
    private String reason;

    public ComplaintModel(){
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getComplaintId() {
        return complaintId;
    }


    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getResolved() {
        return resolved;
    }

    public void setResolved(String resolved) {
        this.resolved = resolved;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        this.resend = resend;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }
}
