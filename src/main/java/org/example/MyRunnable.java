package org.example;

import java.io.IOException;

public class MyRunnable implements Runnable {
    private CrptApi crptApi;

    public MyRunnable(CrptApi crptApi) {
        this.crptApi = crptApi;
    }

    @Override
    public void run() {
        try {
            crptApi.createDocument(new CrptApi.Document(), "signature");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
