package com.ismail.maceraaletleri.item;

// Kanca seviyeleri. Menzil blok, çekme hızı blok/tick cinsinden.
public enum KancaSeviyesi {
    DEMIR(30.0, 0.8, 128),
    ELMAS(45.0, 1.1, 512),
    NETHERITE(60.0, 1.4, 1024);

    private final double menzil;
    private final double cekmeHizi;
    private final int dayaniklilik;

    KancaSeviyesi(double menzil, double cekmeHizi, int dayaniklilik) {
        this.menzil = menzil;
        this.cekmeHizi = cekmeHizi;
        this.dayaniklilik = dayaniklilik;
    }

    public double menzil() {
        return menzil;
    }

    public double cekmeHizi() {
        return cekmeHizi;
    }

    public int dayaniklilik() {
        return dayaniklilik;
    }
}
