package de.uni_hildesheim.sse.kernel_miner.kconfig;

public class KconfigSymbol {

    private KconfigType type;
    
    private String name;
    
    public KconfigSymbol(KconfigType type, String name) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public KconfigType getType() {
        return type;
    }
    
}
