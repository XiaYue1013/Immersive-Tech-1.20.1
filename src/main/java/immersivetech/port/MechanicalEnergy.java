package immersivetech.port;

public record MechanicalEnergy(int torque, int speed) {
    public static final MechanicalEnergy NONE = new MechanicalEnergy(0, 0);

    public int power() {
        return Math.multiplyExact(torque, speed);
    }
}
