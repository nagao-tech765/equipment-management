package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller
 * ブラウザからのリクエストを受け取り、
 * 必要な処理を指示するクラス
 *
 * @AutowiredによってSpringがEquipmentRepositoryを用意し、
 * repositoryに入れる
 */
@Controller
public class HomeController {
    @Autowired
    EquipmentRepository repository;
    
    /*ブラウザからGETで / にアクセスされたらデータベースからEquipmentを全部取得して、
     * listという名前で画面に渡す
     * index.htmlを表示、データを渡す
    */
    @GetMapping("/")
    public String home(Model model) {
    	
        model.addAttribute("list", repository.findAll());

        return "index";
    }
    /*
     * /register にPOSTされたらname,quantityを受け取りDBに保存する
     * quantityが1未満なら最初に戻す
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam Integer quantity) {

        if(quantity < 1){
            return "redirect:/";
        }

        Equipment equipment = new Equipment();

        equipment.setName(name);
        equipment.setQuantity(quantity);

        repository.save(equipment);

        return "redirect:/";
    }
    
    /*
     * /delete にPOSTされたらidを消す
     * @PathVariableでURLに含まれている値を変数に入れる
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        repository.deleteById(id);

        return "redirect:/";
    }
    
    @GetMapping("/search")
    public String search(
            @RequestParam String name,
            Model model) {

        model.addAttribute(
            "list",
            repository.findByNameContaining(name)
        );

        return "index";
    }
    /*
     * ＋を押すとgetQuantityで現在のquantityを取得して＋１して保存
     * そして/に戻る事で更新
     */
    @PostMapping("/quantity/increase/{id}")
    public String increaseQuantity(@PathVariable Integer id) {

        Equipment equipment = repository.findById(id)
                .orElseThrow();

        equipment.setQuantity(equipment.getQuantity() + 1);

        repository.save(equipment);

        return "redirect:/";
    }
    
    @PostMapping("/quantity/decrease/{id}")
    public String decreaseQuantity(@PathVariable Integer id) {

        Equipment equipment = repository.findById(id)
                .orElseThrow();

        if (equipment.getQuantity() > 1) {
            equipment.setQuantity(equipment.getQuantity() - 1);
            repository.save(equipment);
        }

        return "redirect:/";
    }

}