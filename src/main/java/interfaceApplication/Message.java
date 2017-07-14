package interfaceApplication;

import java.util.HashMap;

import org.json.simple.JSONObject;

import json.JSONHelper;
import model.MessageModel;
import nlogger.nlogger;
import security.codec;
import time.TimeHelper;

public class Message {
	private MessageModel model = new MessageModel();
	private HashMap<String, Object> map = new HashMap<>();

	public Message() {
		map.put("messageDate", TimeHelper.nowMillis());
		map.put("floor", model.getFloor());
		map.put("fatherid", 0);
		map.put("oid", "0"); // 文章id
		map.put("state", 0); // 状态
		map.put("replynum", 0); // 留言被回复多少次
		map.put("r", 1000); // 读取 权限值
		map.put("u", 2000); // 修改 权限值
		map.put("d", 3000); // 删除 权限值
	}

	/**
	 * 新增留言及回复留言
	 * 
	 * @param msgInfo
	 * @return 若是新增留言，则存在字段floor，fatherid默认为0，
	 *         若为回复留言，则不存在floor字段，fatherid为需回复的留言的id
	 */
	@SuppressWarnings("unchecked")
	public String AddMessage(String msgInfo) {
		int code = 0;
		JSONObject result = null;
		JSONObject temp = new JSONObject();
		JSONObject object = model.AddMap(map, JSONHelper.string2json(msgInfo));
		if (object != null) {
			try {
				if (object.containsKey("fatherid")) {
					String fatherid = object.get("fatherid").toString();
					if (!fatherid.equals("0")) {
						object.remove("floor");
						String replynum = String.valueOf(model.countReply(fatherid) + 1);
						temp.put("replynum", Integer.parseInt(replynum));
						code = model.updateMessage(object.get("fatherid").toString(), temp);
					}
				}
				if (code == 0) {
					String messageContent = codec.DecodeHtmlTag((String) object.get("messageContent"));
					messageContent = codec.decodebase64(messageContent);
//					object.put("messageContent", messageContent);
					object.escapeHtmlPut("messageContent", messageContent);
					result = new JSONObject();
					String info = model.addMessage(object);
					result = (info != null ? JSONHelper.string2json(info) : null);
				}
			} catch (Exception e) {
				nlogger.logout(e);
				result = null;
			}
		}
		return model.resultMessage(result);
	}

	// 修改留言
	public String UpdateMessage(String mid, String msgInfo) {
		return model.resultMessage(model.updateMessage(mid, JSONHelper.string2json(msgInfo)), "留言修改成功");
	}

	// 删除留言
	public String DeleteMessage(String mid) {
		return model.resultMessage(model.deleteMessage(mid), "删除留言成功");
	}

	// 批量删除留言
	public String DeleteBatchMessage(String mids) {
		return model.resultMessage(model.deleteMessage(mids.split(",")), "批量删除留言成功");
	}

	public String MaskMessage(String mid) {
		return model.resultMessage(model.deletesMessage(mid), "隐藏留言或回复成功");
	}

	// 搜索留言
	public String SearchMessage(String msgInfo) {
		return model.find(JSONHelper.string2json(msgInfo));
	}

	// 分页
	public String PageMessage(int idx, int pageSize) {
		return model.page(idx, pageSize);
	}

	// 条件分页
	public String PageByMessage(int idx, int pageSize, String msgInfo) {
		return model.page(idx, pageSize, JSONHelper.string2json(msgInfo));
	}

	// 获取某篇文章下的留言
	public String getMsgByOid(String oid) {
		return model.FindMsgByOID(oid);
	}

	// 将某一条留言关联至某篇文章
	public String connArc(String mid, String oid) {
		return model.resultMessage(model.setMsgConOID(mid, oid), "文章关联留言成功");
	}
}
