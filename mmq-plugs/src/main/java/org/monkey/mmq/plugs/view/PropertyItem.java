package org.monkey.mmq.plugs.view;

import java.util.List;

/**
 * @ClassName:Item
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/4 23:44
 * @Version: v1.0
 */

public class PropertyItem {
    private String label;
    private String code;
    private ComponentType component;
    private List<KeyValue> value;
    private Object modal;

    public PropertyItem() {

    }

    public PropertyItem(String label, String code, ComponentType component, Object modal) {
        this.label = label;
        this.code = code;
        this.component = component;
        this.value = value;
        this.modal = modal;
    }

    public PropertyItem(String label, String code, ComponentType component, List<KeyValue> value, Object modal) {
        this.label = label;
        this.code = code;
        this.component = component;
        this.value = value;
        this.modal = modal;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String name) {
        this.label = name;
    }

    public List<KeyValue> getValue() {
        return value;
    }

    public void setValue(List<KeyValue> value) {
        this.value = value;
    }

    public ComponentType getComponent() {
        return component;
    }

    public void setComponent(ComponentType component) {
        this.component = component;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getModal() {
        return modal;
    }

    public void setModal(Object modal) {
        this.modal = modal;
    }
}
