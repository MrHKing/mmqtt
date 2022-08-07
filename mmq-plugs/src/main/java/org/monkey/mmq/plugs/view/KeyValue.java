package org.monkey.mmq.plugs.view;

/**
 * @ClassName:Option
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/4 23:44
 * @Version: v1.0
 */

public class KeyValue {
    String key;
    String value;
    public KeyValue() {

    }
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
