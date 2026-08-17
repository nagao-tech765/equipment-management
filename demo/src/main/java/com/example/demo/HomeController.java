package com.example.demo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

		// DBから備品を全部取得
		var equipmentList = repository.findAll();

		// 今日の日付
		LocalDate today = LocalDate.now();

		// すべての備品を1件ずつ確認
		for (Equipment equipment : equipmentList) {

			// 自動減少の設定がない場合は何もしない
			if (equipment.getDecreaseMonths() == null
					|| equipment.getDecreaseQuantity() == null
					|| equipment.getLastDecreasedAt() == null) {

				continue;
			}

			// 最後に減らしてから何ヶ月経ったか計算
			long monthsPassed = ChronoUnit.MONTHS.between(
					equipment.getLastDecreasedAt(),
					today);

			// 設定した期間に達していなければ何もしない
			if (monthsPassed < equipment.getDecreaseMonths()) {
				continue;
			}

			// 何回分の減少が発生したか計算
			long decreaseCount = monthsPassed / equipment.getDecreaseMonths();

			// 合計で何個減らすか計算
			int totalDecrease = (int) (decreaseCount * equipment.getDecreaseQuantity());

			// 数量が0未満にならないようにする
			int newQuantity = Math.max(0, equipment.getQuantity() - totalDecrease);

			// 新しい数量を設定
			equipment.setQuantity(newQuantity);

			// 最後に減らした日を更新
			equipment.setLastDecreasedAt(
					equipment.getLastDecreasedAt()
							.plusMonths(
									decreaseCount * equipment.getDecreaseMonths()));

			// DBに保存
			repository.save(equipment);
		}

		// 更新後のデータを画面に渡す
		model.addAttribute("list", repository.findAllByOrderByQuantityAsc());

		return "index";
	}

	/*
	 * /register にPOSTされたらname,quantityを受け取りDBに保存する
	 */
	@PostMapping("/register")
	public String register(
	        @RequestParam String name,
	        @RequestParam Integer quantity,
	        @RequestParam(required = false) Integer decreaseMonths,
	        @RequestParam(required = false) Integer decreaseQuantity,
	        Model model) {

	    // 数量が1未満ならエラー
	    if (quantity < 1) {
	        model.addAttribute("error", "数量は1以上で入力してください。");
	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());
	        return "index";
	    }

	    // 減少間隔と減少数が片方だけ入力されている場合
	    if ((decreaseMonths == null && decreaseQuantity != null)
	            || (decreaseMonths != null && decreaseQuantity == null)) {

	        model.addAttribute(
	                "error",
	                "減少間隔と減少数は両方入力してください。"
	        );

	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());

	        return "index";
	    }

	    // 減少間隔が1未満ならエラー
	    if (decreaseMonths != null && decreaseMonths < 1) {
	        model.addAttribute(
	                "error",
	                "減少間隔は1以上で入力してください。"
	        );

	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());

	        return "index";
	    }

	    // 減少数が1未満ならエラー
	    if (decreaseQuantity != null && decreaseQuantity < 1) {
	        model.addAttribute(
	                "error",
	                "減少数は1以上で入力してください。"
	        );

	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());

	        return "index";
	    }

	    Equipment equipment = new Equipment();

	    equipment.setName(name);
	    equipment.setQuantity(quantity);

	    // 自動減少の設定
	    equipment.setDecreaseMonths(decreaseMonths);
	    equipment.setDecreaseQuantity(decreaseQuantity);

	    // 自動減少を設定した場合だけ基準日を設定
	    if (decreaseMonths != null && decreaseQuantity != null) {

	        // 登録した日を基準日にする
	        equipment.setLastDecreasedAt(LocalDate.now());
	    }

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
				repository.findByNameContaining(name));

		return "index";
	}

	/*
	 * ＋を押すとgetQuantityで現在のquantityを取得して＋１して保存
	 * そして/に戻る事で更新
	 */
	/*@PostMapping("/quantity/increase/{id}")
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
	}*/
	
	@PostMapping("/quantity/update/{id}")
	public String updateQuantity(
			@PathVariable Integer id,
			@RequestParam Integer quantity) {
		
		Equipment equipment = repository.findById(id)
				.orElseThrow();
		
		//1未満になることを防ぐ
		if(quantity < 1) {
			quantity = 1;
		}
		
		equipment.setQuantity(quantity);
		
		repository.save(equipment);
		
		return "redirect:/";
		
	}

}