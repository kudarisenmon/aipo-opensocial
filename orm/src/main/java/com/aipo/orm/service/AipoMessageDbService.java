/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aipo.orm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.cayenne.DataRow;

import com.aipo.orm.Database;
import com.aipo.orm.model.portlet.EipTMessage;
import com.aipo.orm.model.portlet.EipTMessageRead;
import com.aipo.orm.model.portlet.EipTMessageRoom;
import com.aipo.orm.model.portlet.EipTMessageRoomMember;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.query.SQLTemplate;
import com.aipo.orm.service.request.SearchOptions;
import com.aipo.util.CommonUtils;
import com.google.inject.Inject;

/**
 *
 */
public class AipoMessageDbService implements MessageDbService {

  private final TurbineUserDbService turbineUserDbService;

  @Inject
  public AipoMessageDbService(TurbineUserDbService turbineUserDbService) {
    this.turbineUserDbService = turbineUserDbService;
  }

  /**
   * @param username
   * @param options
   * @return
   */
  @Override
  public List<EipTMessageRoom> findMessageRoom(int roomId, String username,
      SearchOptions options) {
    TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
    if (turbineUser == null) {
      return new ArrayList<EipTMessageRoom>();
    }

    // Filter
    String filter = options.getFilterBy();
    // FilterOperation filterOperation = options.getFilterOperation();
    String filterValue = options.getFilterValue();
    boolean isFilter = false;

    // MessageUtils.getRoomListと同等の処理（pageの制約はなし）に必要な変数を取得
    Integer user_id = turbineUser.getUserId();
    // int limit = options.getLimit();
    boolean isMySQL = !Database.isJdbcPostgreSQL();

    StringBuilder select = new StringBuilder();

    select.append("select");
    select.append(" t2.room_id, ");
    select.append(" t2.name, ");
    select.append(" t2.has_photo, ");
    select.append(" t2.photo_modified, ");
    select.append(" t4.user_id, ");
    select.append(" t4.login_name, ");
    select.append(" t4.last_name, ");
    select.append(" t4.first_name, ");
    select.append(" t4.has_photo as user_has_photo, ");
    select.append(" t4.photo_modified as user_photo_modified, ");
    select.append(" t2.auto_name, ");
    select.append(" t2.room_type, ");

    select.append(" t2.last_message, ");
    select.append(" last_update_date, ");
    select
      .append(" (select count(*) from eip_t_message_read t3 where t3.room_id = t2.room_id and t3.user_id = #bind($user_id) and t3.is_read ='F') as unread ");

    StringBuilder count = new StringBuilder();
    count.append("select count(t2.room_id) AS c ");

    StringBuilder body = new StringBuilder();
    body
      .append("  from eip_t_message_room_member t1, eip_t_message_room t2, turbine_user t4 where t1.user_id = #bind($user_id) and t1.room_id = t2.room_id and t1.target_user_id = t4.user_id ");

    if (roomId > 0) {
      body.append(" and t1.room_id=");
      body.append(roomId);
    }

    if ("keyword".equals(filter)) {
      if (isMySQL) {
        body
          .append(" and ((t2.room_type='G' and t2.name like #bind($keyword)) or (t2.room_type='O' and CONCAT(t4.last_name,t4.first_name) like #bind($keyword))) ");
      } else {
        body
          .append(" and ((t2.room_type='G' and t2.name like #bind($keyword)) or (t2.room_type='O' and (t4.last_name || t4.first_name) like #bind($keyword))) ");
      }
      isFilter = true;
    }

    StringBuilder last = new StringBuilder();

    last.append(" order by t2.last_update_date desc ");
    /*-
    if (limit > 0) {
      last.append(" LIMIT ");
      last.append(limit);
    }
     */

    SQLTemplate<EipTMessageRoom> sql =
      Database.sql(
        EipTMessageRoom.class,
        select.toString() + body.toString() + last.toString()).param(
        "user_id",
        Integer.valueOf(user_id));
    if (isFilter) {
      sql.param("keyword", "%" + filterValue + "%");
    }
    List<DataRow> fetchList = sql.fetchListAsDataRow();

    List<EipTMessageRoomMember> roomMembers = null;
    if (roomId > 0) {
      roomMembers =
        Database
          .sql(
            EipTMessageRoomMember.class,
            "select login_name from eip_t_message_room_member where room_id=#bind($room_id)")
          .param("room_id", Integer.valueOf(roomId))
          .fetchList();
    }

    List<EipTMessageRoom> list = new ArrayList<EipTMessageRoom>();
    for (DataRow row : fetchList) {
      Long unread = (Long) row.get("unread");
      Integer tUserId = (Integer) row.get("user_id");
      String loginName = (String) row.get("login_name");
      String lastName = (String) row.get("last_name");
      String firstName = (String) row.get("first_name");
      String hasPhoto = (String) row.get("user_has_photo");
      Date photoModified = (Date) row.get("user_photo_modified");

      EipTMessageRoom object =
        Database.objectFromRowData(row, EipTMessageRoom.class);
      object.setUnreadCount(unread.intValue());
      object.setUserId(tUserId);
      object.setLoginName(loginName);
      object.setFirstName(firstName);
      object.setLastName(lastName);
      if (roomId > 0 && roomMembers != null) {
        List<String> roomMembersStr = new ArrayList();
        for (EipTMessageRoomMember roomMember : roomMembers) {
          roomMembersStr.add(roomMember.getLoginName());
        }
        object.setRoomMembers(roomMembersStr);

      }
      object.setUserHasPhoto(hasPhoto);
      if (photoModified != null) {
        object.setUserPhotoModified(photoModified.getTime());
      }
      list.add(object);
    }

    return list;

  }

  /**
   * @param roomId
   * @param messageId
   * @param options
   * @return
   */
  @Override
  public List<EipTMessage> findMessage(int roomId, int messageId,
      SearchOptions options) {
    int limit = options.getLimit();
    Integer untilId = options.getParameterInt("untilId");
    Integer sinceId = options.getParameterInt("sinceId");
    String keyword = options.getParameter("keyword");

    StringBuilder select = new StringBuilder();

    select.append("select");
    select.append(" t1.message_id, ");
    select.append(" t1.room_id,  ");
    select.append(" t1.user_id, ");
    select.append(" t1.message, ");
    select.append(" t1.create_date, ");
    select.append(" t1.member_count, ");
    select.append(" t2.login_name, ");
    select.append(" t2.last_name, ");
    select.append(" t2.first_name, ");
    select.append(" t2.has_photo, ");
    select.append(" t2.photo_modified, ");

    select
      .append(" (select count(*) from eip_t_message_read t3 where t3.message_id = t1.message_id and t3.room_id = t1.room_id and t3.is_read = 'F') as unread ");

    StringBuilder count = new StringBuilder();
    count.append("select count(t1.message_id) AS c ");

    StringBuilder body = new StringBuilder();
    body
      .append("  from eip_t_message t1, turbine_user t2 where t1.user_id = t2.user_id and t1.room_id = #bind($room_id) ");
    if (untilId != null && untilId > 0) {
      body.append(" and t1.message_id<");
      body.append(untilId);
    }
    if (sinceId != null && sinceId > 0) {
      body.append(" and t1.message_id>");
      body.append(sinceId);
    }
    if (messageId > 0) {
      body.append(" and t1.message_id=");
      body.append(messageId);
    }

    StringBuilder last = new StringBuilder();

    last.append(" order by t1.create_date desc ");

    if (limit > 0) {
      last.append(" limit ");
      last.append(limit);
    }

    List<DataRow> fetchList =
      Database.sql(
        EipTMessage.class,
        select.toString() + body.toString() + last.toString()).param(
        "room_id",
        Integer.valueOf(roomId)).fetchListAsDataRow();

    List<EipTMessageRoomMember> roomMembers =
      Database
        .sql(
          EipTMessageRoomMember.class,
          "select login_name from eip_t_message_room_member where room_id=#bind($room_id)")
        .param("room_id", Integer.valueOf(roomId))
        .fetchList();

    List<EipTMessage> list = new ArrayList<EipTMessage>();
    for (DataRow row : fetchList) {
      Long unread = (Long) row.get("unread");
      String loginName = (String) row.get("login_name");
      String lastName = (String) row.get("last_name");
      String firstName = (String) row.get("first_name");
      String hasPhoto = (String) row.get("has_photo");
      Date photoModified = (Date) row.get("photo_modified");

      EipTMessage object = Database.objectFromRowData(row, EipTMessage.class);
      object.setUnreadCount(unread.intValue());
      object.setLoginName(loginName);
      object.setFirstName(firstName);
      object.setLastName(lastName);
      object.setHasPhoto(hasPhoto);
      object.setRoomId(roomId);
      List<String> roomMembersStr = new ArrayList();
      for (EipTMessageRoomMember roomMember : roomMembers) {
        roomMembersStr.add(roomMember.getLoginName());
      }
      object.setRoomMembers(roomMembersStr);
      if (photoModified != null) {
        object.setPhotoModified(photoModified.getTime());
      }
      list.add(object);
    }
    return list;
  }

  /**
   * @param roomId
   * @param targetUserId
   * @param message
   * @param fields
   */
  @Override
  public void createMessage(String username, Integer roomId,
      String targetUsername, String message, Set<String> fields) {
    try {
      TurbineUser turbineUser = turbineUserDbService.findByUsername(username);
      TurbineUser targetUser =
        turbineUserDbService.findByUsername(targetUsername);
      Date now = new Date();

      if (roomId == null && targetUsername != null) {
        // int userId = (int) login_user.getUserId().getValue();
        // int targetUserId = (int) targetUser.getUserId().getValue();
        // room = MessageUtils.getRoom(userId, targetUserId);
        // if (room == null) {
        // room = Database.create(EipTMessageRoom.class);
        //
        // EipTMessageRoomMember map1 =
        // Database.create(EipTMessageRoomMember.class);
        // map1.setEipTMessageRoom(room);
        // map1.setUserId((int) login_user.getUserId().getValue());
        // map1.setTargetUserId((int) targetUser.getUserId().getValue());
        // map1.setLoginName(login_user.getName().getValue());
        //
        // EipTMessageRoomMember map2 =
        // Database.create(EipTMessageRoomMember.class);
        // map2.setEipTMessageRoom(room);
        // map2.setTargetUserId((int) login_user.getUserId().getValue());
        // map2.setUserId((int) targetUser.getUserId().getValue());
        // map2.setLoginName(targetUser.getName().getValue());
        //
        // room.setAutoName("T");
        // room.setRoomType("O");
        // room.setLastUpdateDate(now);
        // room.setCreateDate(now);
        // room.setCreateUserId((int) login_user.getUserId().getValue());
        // room.setUpdateDate(now);
        //
        // Database.commit();
        // }
      }
      // if (room == null) {
      // throw new IllegalArgumentException("room may not be null. ");
      // }
      @SuppressWarnings("unchecked")
      EipTMessageRoom room =
        Database.get(EipTMessageRoom.class, roomId.longValue());
      List<EipTMessageRoomMember> members = room.getEipTMessageRoomMember();

      EipTMessage model = Database.create(EipTMessage.class);
      model.setEipTMessageRoom(room);
      model.setMessage(CommonUtils.removeSpace(message));
      model.setCreateDate(now);
      model.setUpdateDate(now);
      model.setMemberCount(members.size());
      model.setUserId((int) turbineUser.getUserId());

      List<String> recipients = new ArrayList<String>();
      for (EipTMessageRoomMember member : members) {
        if (member.getUserId().intValue() != turbineUser.getUserId()) {
          EipTMessageRead record = Database.create(EipTMessageRead.class);
          record.setEipTMessage(model);
          record.setIsRead("F");
          record.setUserId(member.getUserId());
          record.setRoomId(room.getRoomId());
          recipients.add(member.getLoginName());
        }
      }

      room.setLastMessage(CommonUtils.compressString(message, 100));
      room.setLastUpdateDate(now);

      // TODO:ファイル添付機能の追加
      // insertAttachmentFiles(fileuploadList, folderName, (int) login_user
      // .getUserId()
      // .getValue(), model, msgList);

      Database.commit();

      // TODO:プッシュ通知機能の追加
      // Map<String, String> params = new HashMap<String, String>();
      // params.put("roomId", String.valueOf(room.getRoomId()));
      // params.put("messageId", String.valueOf(model.getMessageId()));
      // ALPushService.pushAsync("messagev2", params, recipients);

    } catch (Exception ex) {
      Database.rollback();
    }
  }
}