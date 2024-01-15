package foundry.veil.api;

public interface ChargableItem {

    int getMaxCharge();

    int getCharge();

    void setCharge(int charge);

    void addCharge(int charge);

    void removeCharge(int charge);
}
