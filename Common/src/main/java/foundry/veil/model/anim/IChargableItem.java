package foundry.veil.model.anim;

public interface IChargableItem {
    int getMaxCharge();
    int getCharge();
    void setCharge(int charge);
    void addCharge(int charge);
    void removeCharge(int charge);
}
