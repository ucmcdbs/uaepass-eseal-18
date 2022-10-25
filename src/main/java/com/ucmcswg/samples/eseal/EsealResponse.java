package com.ucmcswg.samples.eseal;

public class EsealResponse {
    private String status;
    private byte[] data;

    public boolean isSuccess;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EsealResponse{" +
                ", status=" + status +
                ", data=" + data +
                '}';
    }
}
