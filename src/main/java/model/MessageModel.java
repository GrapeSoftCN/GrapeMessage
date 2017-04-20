package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import esayhelper.DBHelper;
import esayhelper.formHelper;
import esayhelper.formHelper.formdef;
import esayhelper.jGrapeFW_Message;

public class MessageModel {
	private static DBHelper msg;
	private static formHelper _form;
	static {
		msg = new DBHelper("mongodb", "message");
		_form = msg.getChecker();
	}

	public MessageModel(){
		_form.putRule("messageContent", formdef.notNull);
	}
	public String addMessage(JSONObject object) {
		if (!_form.checkRuleEx(object)) {
			return resultMessage(1, ""); // 必填字段没有填
		}
		String info = msg.data(object).insertOnce().toString();
		return FindMsgByID(info).toString();
	}

	public int updateMessage(String mid, JSONObject object) {
		return msg.eq("_id", new ObjectId(mid)).data(object).update() != null ? 0 : 99;
	}

	public int deleteMessage(String mid) {
		return msg.eq("_id", new ObjectId(mid)).delete() != null ? 0 : 99;
	}

	public int deleteMessage(String[] mids) {
		msg = (DBHelper) msg.or();
		for (int i = 0; i < mids.length; i++) {
			msg.eq("_id", new ObjectId(mids[i]));
		}
		return msg.delete() != null ? 0 : 99;
	}

	public JSONArray find(JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			msg.eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		return msg.select();
	}
	public JSONObject find(String mid) {
		return msg.eq("_id", new ObjectId(mid)).field("replynum").find();
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize) {
		JSONArray array = msg.page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize", (int) Math.ceil((double) msg.count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize, JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			msg.eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		JSONArray array = msg.page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize", (int) Math.ceil((double) msg.count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	// 查询某篇文章下所有的留言
	public JSONArray FindMsgByOID(String oid) {
		return msg.eq("oid", oid).select();
	}

	// 查询某篇文章下所有的留言
	@SuppressWarnings("unchecked")
	public int setMsgConOID(String mid,String oid) {
		JSONObject object = new JSONObject();
		object.put("oid", oid);
		return msg.eq("_id", new ObjectId(mid)).data(object).update()!=null?0:99;
	}

	/**
	 * 通过唯一标识符_id,查询留言信息
	 * 
	 * @param mid
	 * @return
	 */
	public JSONObject FindMsgByID(String mid) {
		return msg.eq("_id", new ObjectId(mid)).find();
	}

	public long countReply(String fid) {
		return msg.eq("fatherid", fid).count()+1;
	}
	public int getFloor() {
		return Integer.parseInt(msg._count()) + 1;
	}

	public String getID() {
		String str = UUID.randomUUID().toString();
		return str.replace("-", "");
	}

	/**
	 * 将map添加至JSONObject中
	 * 
	 * @param map
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject AddMap(HashMap<String, Object> map, JSONObject object) {
		if (map.entrySet() != null) {
			Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				if (!object.containsKey(entry.getKey())) {
					object.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return object;
	}

	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填项没有填";
			break;
		default:
			msg = "其它异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
