package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.jGrapeFW_Message;
import apps.appsProxy;
import check.formHelper;
import check.formHelper.formdef;
import database.DBHelper;
import database.db;
import nlogger.nlogger;

public class MessageModel {
	private DBHelper msg;
	private formHelper _form;
	private JSONObject _obj = new JSONObject();


	public MessageModel() {
		msg = new DBHelper(appsProxy.configValue().get("db").toString(), "message");
		_form = msg.getChecker();
		_form.putRule("messageContent", formdef.notNull);
	}

	private db bind() {
		return msg.bind(String.valueOf(appsProxy.appid()));
	}

	public String addMessage(JSONObject object) {
		String info = "";
		if (object != null) {
			if (!_form.checkRuleEx(object)) {
				return resultMessage(1, ""); // 必填字段没有填
			}
			info = bind().data(object).insertOnce().toString();
		}
		if (("").equals(info)) {
			return resultMessage(99);
		}
		JSONObject obj = FindMsgByID(info);
		return obj != null ? obj.toString() : null;
	}

	public int updateMessage(String mid, JSONObject object) {
		int code = 99;
		if (object != null) {
			code = bind().eq("_id", new ObjectId(mid)).data(object).update() != null ? 0 : 99;
		}
		return code;
	}

	public int deleteMessage(String mid) {
		if (mid.contains(",")) {
			return 99;
		}
		return bind().eq("_id", new ObjectId(mid)).delete() != null ? 0 : 99;
	}

	@SuppressWarnings("unchecked")
	public int deletesMessage(String mid) {
		JSONObject obj = new JSONObject();
		obj.put("isdelete", "1");
		return bind().eq("_id", new ObjectId(mid)).data(obj).update() != null ? 0 : 99;
	}

	public int deleteMessage(String[] mids) {
		bind().or();
		for (int i = 0; i < mids.length; i++) {
			bind().eq("_id", new ObjectId(mids[i]));
		}
		return bind().deleteAll() == mids.length ? 0 : 99;
	}

	public String find(JSONObject fileInfo) {
		JSONArray array = null;
		try {
			array = new JSONArray();
			for (Object object2 : fileInfo.keySet()) {
				bind().eq(object2.toString(), fileInfo.get(object2.toString()));
			}
			array = bind().limit(20).select();
		} catch (Exception e) {
			nlogger.logout(e);
			array = null;
		}

		return resultMessage(array);
	}

	public JSONObject find(String mid) {
		JSONObject object = bind().eq("_id", new ObjectId(mid)).field("replynum").find();
		return object != null ? object : null;
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize) {
		JSONObject object = null;
		try {
			JSONArray array = bind().page(idx, pageSize);
			object = new JSONObject();
			object.put("totalSize", (int) Math.ceil((double) bind().count() / pageSize));
			object.put("currentPage", idx);
			object.put("pageSize", pageSize);
			object.put("data", dencode(array));
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}

		return resultMessage(object);
	}

	@SuppressWarnings("unchecked")
	public String page(int idx, int pageSize, JSONObject fileInfo) {
		JSONObject object = null;
		if (fileInfo != null) {
			try {
				for (Object object2 : fileInfo.keySet()) {
					bind().eq(object2.toString(), fileInfo.get(object2.toString()));
				}
				JSONArray array = bind().dirty().page(idx, pageSize);
				object = new JSONObject();
				object.put("totalSize", (int) Math.ceil((double) bind().count() / pageSize));
				object.put("currentPage", idx);
				object.put("pageSize", pageSize);
				object.put("data", dencode(array));
			} catch (Exception e) {
				nlogger.logout(e);
				object = null;
			}finally {
				bind().clear();
			}
		}
		return resultMessage(object);
	}

	// 查询某篇文章下所有的留言
	public String FindMsgByOID(String oid) {
		JSONArray array = null;
		try {
			array = new JSONArray();
			array = bind().eq("oid", oid).limit(20).select();
		} catch (Exception e) {
			nlogger.logout(e);
			array = null;
		}
		return resultMessage(array);
	}

	// 文章与留言关联
	@SuppressWarnings("unchecked")
	public int setMsgConOID(String mid, String oid) {
		JSONObject object = new JSONObject();
		object.put("oid", oid);
		return bind().eq("_id", new ObjectId(mid)).data(object).update() != null ? 0 : 99;
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
		JSONObject object = bind().eq("_id", new ObjectId(mid)).find();
		return object != null ? object : null;
	}

	public long countReply(String fid) {
		long code = 0;
		try {
			code = bind().eq("fatherid", fid).count();
		} catch (Exception e) {
			nlogger.logout(e);
			code = 0;
		}
		return code;
	}

	public int getFloor() {
		int floor = 0;
		try {
			floor = Integer.parseInt(String.valueOf(bind().count())) + 1;
		} catch (Exception e) {
			nlogger.logout(e);
			floor = 0;
		}
		return floor;
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
		if (object != null) {
			if (map.entrySet() != null) {
				Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
					if (!object.containsKey(entry.getKey())) {
						object.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	private JSONArray dencode(JSONArray array) {
		if (array.size() == 0) {
			return array;
		}
		for (int i = 0; i < array.size(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			if (object.containsKey("content") && object.get("content") != "") {
				object.put("content", object.escapeHtmlGet("content"));
			}
			array.set(i, object);
		}
		return array;
	}
	private String resultMessage(int num) {
		return resultMessage(num, "");
	}

	@SuppressWarnings("unchecked")
	public String resultMessage(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
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
