package org.monkey.mmq.plugs.view;

import java.util.Date;
import java.util.List;

/**
 * @ClassName:LicenseView
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/4 23:05
 * @Version: v1.0
 */

public class MmqPlugsView {
    /**
     * 插件名称
     */
    String plugsName;
    /**
     * 插件代码
     */
    String plugsCode;
    /**
     * is enable
     */
    boolean enable;
    /**
     * 属性
     */
    List<PropertyItem> propertyItems;

    public String getPlugsName() {
        return plugsName;
    }

    public void setPlugsName(String plugsName) {
        this.plugsName = plugsName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<PropertyItem> getPropertyItems() {
        return propertyItems;
    }

    public void setPropertyItems(List<PropertyItem> propertyItems) {
        this.propertyItems = propertyItems;
    }

    public String getPlugsCode() {
        return plugsCode;
    }

    public void setPlugsCode(String plugsCode) {
        this.plugsCode = plugsCode;
    }
}
