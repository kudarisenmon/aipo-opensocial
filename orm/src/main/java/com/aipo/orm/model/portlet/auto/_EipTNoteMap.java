package com.aipo.orm.model.portlet.auto;

import java.util.Date;

import org.apache.cayenne.CayenneDataObject;

import com.aipo.orm.model.portlet.EipTNote;

/**
 * Class _EipTNoteMap was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _EipTNoteMap extends CayenneDataObject {

    public static final String CONFIRM_DATE_PROPERTY = "confirmDate";
    public static final String DEL_FLG_PROPERTY = "delFlg";
    public static final String NOTE_ID_PROPERTY = "noteId";
    public static final String NOTE_STAT_PROPERTY = "noteStat";
    public static final String USER_ID_PROPERTY = "userId";
    public static final String EIP_TNOTE_PROPERTY = "eipTNote";

    public static final String ID_PK_COLUMN = "ID";

    public void setConfirmDate(Date confirmDate) {
        writeProperty("confirmDate", confirmDate);
    }
    public Date getConfirmDate() {
        return (Date)readProperty("confirmDate");
    }

    public void setDelFlg(String delFlg) {
        writeProperty("delFlg", delFlg);
    }
    public String getDelFlg() {
        return (String)readProperty("delFlg");
    }

    public void setNoteId(Integer noteId) {
        writeProperty("noteId", noteId);
    }
    public Integer getNoteId() {
        return (Integer)readProperty("noteId");
    }

    public void setNoteStat(String noteStat) {
        writeProperty("noteStat", noteStat);
    }
    public String getNoteStat() {
        return (String)readProperty("noteStat");
    }

    public void setUserId(String userId) {
        writeProperty("userId", userId);
    }
    public String getUserId() {
        return (String)readProperty("userId");
    }

    public void setEipTNote(EipTNote eipTNote) {
        setToOneTarget("eipTNote", eipTNote, true);
    }

    public EipTNote getEipTNote() {
        return (EipTNote)readProperty("eipTNote");
    }


}
