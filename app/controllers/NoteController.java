package controllers;

import java.util.Map;

import org.codehaus.jackson.JsonNode;

import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Note;
import models.NoteItem;
import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import play.*;
import play.data.DynamicForm;
import play.data.DynamicForm.Dynamic;
import play.data.Form;
import play.libs.Json;
import play.modules.mongodb.jackson.MongoDB;
import play.mvc.*;
import views.html.note.*;

public class NoteController extends Controller {
	
	public static DynamicForm dynamicForm = Form.form();
	private static JacksonDBCollection<models.Note, String> notes = MongoDB.collection(models.Note.class, String.class,play.api.Play.current());
	public static Result create() {
		
		Note newNote = new Note();
		newNote.Title = "New Note";
		notes.save(newNote).getSavedId();
		
		return redirect("/");
	}
	
	public static Result get(Integer id){
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result save(){
		Map<String,String> data = dynamicForm.bindFromRequest().data();
		
		String id = data.get("id").toString();
		String title = data.get("title").toString();
		Note fromDB = notes.findOneById(id);
		fromDB.Title = title;
		
		notes.save(fromDB);
	
		com.fasterxml.jackson.databind.JsonNode result = Json.toJson(fromDB);
		//result.put("status", "success");
		//if(isXmlHttp()){
			return ok(result);
		//} else {
		//	return redirect("/");
		//}
	}
	
	public static Result delete(String nodeid){
		notes.removeById(nodeid);
		return redirect("/");
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result saveposition(){
		Map<String,String> data = dynamicForm.bindFromRequest().data();
		String id = data.get("id");
		String left = data.get("left");
		String top = data.get("top");
		
		notes.updateById(id, DBUpdate.set("left", Math.floor(Double.parseDouble(left))).set("top", Math.floor( Double.parseDouble(top))));
		
		Note note = notes.findOneById(id);
		com.fasterxml.jackson.databind.JsonNode result = Json.toJson(note);
		//result.put("status", "success");
		//if(isXmlHttp()){
			return ok(result);
		//} else {
		//	return redirect("/");
		//}
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result savedimension(){
		Map<String,String> data = dynamicForm.bindFromRequest().data();
		String id = data.get("id");
		String height = data.get("height");
		String width = data.get("width");
		
		notes.updateById(id, DBUpdate.set("height", Math.floor(Double.parseDouble(height))).set("width", Math.floor( Double.parseDouble(width))));
		Note note = notes.findOneById(id);
		
		com.fasterxml.jackson.databind.JsonNode result = Json.toJson(note);
		//if(isXmlHttp()){
			return ok(result);
		//} else {
		//	return redirect("/");
		//}
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result getNote(String noteid){
		
		Note note = notes.findOneById(noteid);
		
		com.fasterxml.jackson.databind.JsonNode result = Json.toJson(note);
		//if(isXmlHttp()){
			return ok(result);
		//} else {
		//	return redirect("/");
		//}
	}

	public static Result saveitem(String noteid){
		Map<String,String> data = dynamicForm.bindFromRequest().data();
		String text = data.get("notetext");
		
		int itemid = 0;
		String itemidstr = data.get("itemid");
		if(itemidstr != null && isNumeric(itemidstr)){
			itemid = Integer.parseInt(itemidstr);
		}
		Note fromDB = notes.findOneById(noteid);
		NoteItem item = null;
	
		if(itemid == 0){
			if(text.length() > 0){
				item = new NoteItem();
				item.Id = fromDB.Items.size()+1;
				fromDB.Items.add(item);
			}
		} else {	
			
			for(NoteItem ni : fromDB.Items){
				if(ni != null && ni.Id != null && ni.Id == itemid){
					item = ni;
					break;
				}
			}			
		}
		if(item != null){
			if(text.length() > 0){
				item.NoteText = text;
			} else {
				fromDB.Items.remove(item);
			}
			notes.save(fromDB);
		}
		com.fasterxml.jackson.databind.JsonNode result = Json.toJson(fromDB);
		//if(isXmlHttp()){
			return ok(result);
		//} else {
		//	return redirect("/");
		//}
	}
    public static boolean isNumeric(String s) {  
        return java.util.regex.Pattern.matches("\\d+", s);  
    }  
    
    @BodyParser.Of(BodyParser.Json.class)
    public static Result json() {
    	JacksonDBCollection<Note, String> coll = MongoDB.collection(Note.class, String.class, play.api.Play.current());
    	com.fasterxml.jackson.databind.JsonNode result = Json.toJson(coll.find().toArray());
    	
    	return ok(result);
    }
    
    private static boolean isXmlHttp(){
    	return request().getHeader("X-Requested-With") != null
            	&& request().getHeader("X-Requested-With").equals("XMLHttpRequest");
    }
}
