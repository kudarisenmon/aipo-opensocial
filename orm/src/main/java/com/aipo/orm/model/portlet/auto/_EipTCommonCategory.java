package com.aipo.orm.model.portlet.auto;

import java.util.Date;
import java.util.List;

import org.apache.cayenne.CayenneDataObject;

import com.aipo.orm.model.portlet.EipTScheduleMap;

/**
 * Class _EipTCommonCategory was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _EipTCommonCategory extends CayenneDataObject {

    public static final String CREATE_DATE_PROPERTY = "createDate";
    public static final String CREATE_USER_ID_PROPERTY = "createUserId";
    public static final String NAME_PROPERTY = "name";
    public static final String NOTE_PROPERTY = "note";
    public static final String UPDATE_DATE_PROPERTY = "updateDate";
    public static final String UPDATE_USER_ID_PROPERTY = "updateUserId";
    public static final String EIP_TSCHEDULE_MAPS_PROPERTY = "eipTScheduleMaps";

    public static final String COMMON_CATEGORY_ID_PK_COLUMN = "COMMON_CATEGORY_ID";

    public void setCreateDate(Date createDate) {
        writeProperty("createDate", createDate);
    }
    public Date getCreateDate() {
        return (Date)readProperty("createDate");
    }

    public void setCreateUserId(Integer createUserId) {
        writeProperty("createUserId", createUserId);
    }
    public Integer getCreateUserId() {
        return (Integer)readProperty("createUserId");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setNote(String note) {
        writeProperty("note", note);
    }
    public String getNote() {
        return (String)readProperty("note");
    }

    public void setUpdateDate(Date updateDate) {
        writeProperty("updateDate", updateDate);
    }
    public Date getUpdateDate() {
        return (Date)readProperty("updateDate");
    }

    public void setUpdateUserId(Integer updateUserId) {
        writeProperty("updateUserId", updateUserId);
    }
    public Integer getUpdateUserId() {
        return (Integer)readProperty("updateUserId");
    }

    public void addToEipTScheduleMaps(EipTScheduleMap obj) {
        addToManyTarget("eipTScheduleMaps", obj, true);
    }
    public void removeFromEipTScheduleMaps(EipTScheduleMap obj) {
        removeToManyTarget("eipTScheduleMaps", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<EipTScheduleMap> getEipTScheduleMaps() {
        return (List<EipTScheduleMap>)readProperty("eipTScheduleMaps");
    }


}
