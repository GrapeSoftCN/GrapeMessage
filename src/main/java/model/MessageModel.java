package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import apps.appsProxy;
import database.db;
import esayhelper.DBHelper;
import esayhelper.formHelper;
import esayhelper.formHelper.formdef;
import esayhelper.jGrapeFW_Message;

public class MessageModel {
	private static DBHelper msg;
	private static formHelper _form;
	private JSONObject _obj = new JSONObject();

	static {
		msg = new DBHelper(appsProxy.configValue().get("db").toString(),
				"message");
		// msg = new DBHelper("mongodb", "message");
		_form = msg.getChecker();
	}

	public MessageModel() {
		_form.putRule("messageContent", formdef.notNull);
	}
	private db bind(){
		return msg.bind(String.valueOf(appsProxy.appid()));
	}
	
	public String addMessage(JSONObject object) {
		if (!_form.checkRuleEx(object)) {
			return resultMessage(1, ""); // 必填字段没有填
		}
		String info = bind().data(object).insertOnce().toString();
		return FindMsgByID(info).toString();
	}

	public int updateMessage(String mid, JSONObject object) {
		return bind().eq("_id", new ObjectId(mid)).data(object).update() != null
				? 0 : 99;
	}

	public int deleteMessage(String mid) {
		return bind().eq("_id", new ObjectId(mid)).delete() != null ? 0 : 99;
	}

	@SuppressWarnings("unchecked")
	public int deletesMessage(String mid) {
		JSONObject obj = new JSONObject();
		obj.put("isdelete", "1");
		return bind().eq("_id", new ObjectId(mid)).data(obj).update() != null ? 0
				: 99;
	}

	public int deleteMessage(String[] mids) {
		bind().or();
		for (int i = 0; i < mids.length; i++) {
			bind().eq("_id", new ObjectId(mids[i]));
		}
		return bind().deleteAll() == mids.length ? 0 : 99;
	}

	public JSONArray find(JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			bind().eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		return bind().limit(20).select();
	}

	public JSONObject find(String mid) {
		return bind().eq("_id", new ObjectId(mid)).field("replynum").find();
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize) {
		JSONArray array = bind().page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) bind().count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	@SuppressWarnings("unchecked")
	public JSONObject page(int idx, int pageSize, JSONObject fileInfo) {
		for (Object object2 : fileInfo.keySet()) {
			bind().eq(object2.toString(), fileInfo.get(object2.toString()));
		}
		JSONArray array = bind().dirty().page(idx, pageSize);
		JSONObject object = new JSONObject();
		object.put("totalSize",
				(int) Math.ceil((double) bind().count() / pageSize));
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("data", array);
		return object;
	}

	// 查询某篇文章下所有的留言
	public JSONArray FindMsgByOID(String oid) {
		return bind().eq("oid", oid).limit(20).select();
	}

	// 文章与留言关联
	@SuppressWarnings("unchecked")
	public int setMsgConOID(String mid, String oid) {
		JSONObject object = new JSONObject();
		object.put("oid", oid);
		return bind().eq("_id", new ObjectId(mid)).data(object).update() != null
				? 0 : 99;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getPLV(String mid) {
		JSONObject object = find(mid);
		JSONObject _oObject = new JSONObject();
		_oObject.put("update", object.get("uPlv").toString());
		_oObject.put("delete", object.get("dPlv").toString());
		_oObject.put("read", object.get("rPlv").toString());
		return _oObject;
	}

	/**
	 * 通过唯一标识符_id,查询留言信息
	 * 
	 * @param mid
	 * @return
	 */
	public JSONObject FindMsgByID(String mid) {
		return bind().eq("_id", new ObjectId(mid)).find();
	}

	public long countReply(String fid) {
		return bind().eq("fatherid", fid).count();
	}

	public int getFloor() {
		return Integer.parseInt(String.valueOf(bind().count())) + 1;
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
			Iterator<Entry<String, Object>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator
						.next();
				if (!object.containsKey(entry.getKey())) {
					object.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public String resultMessage(JSONObject object) {
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	@SuppressWarnings("unchecked")
	public String resultMessage(JSONArray array) {
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
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
		case 2:
			msg = "没有创建数据权限，请联系管理员进行权限调整";
			break;
		case 3:
			msg = "没有修改数据权限，请联系管理员进行权限调整";
			break;
		case 4:
			msg = "没有删除数据权限，请联系管理员进行权限调整";
			break;
		default:
			msg = "其它异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
}
