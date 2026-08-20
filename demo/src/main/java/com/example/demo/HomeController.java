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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
			if (equipment.getDecreaseInterval() == null
					|| equipment.getDecreaseQuantity() == null
					|| equipment.getDecreaseUnit() == null
					|| equipment.getLastDecreasedAt() == null) {

				continue;
			}

			// 最後に減らしてからどのくらい経ったかを計算(日数、月数)
			long passed;
			
			if("DAY".equals(equipment.getDecreaseUnit())) {
				//日単位
				passed = ChronoUnit.DAYS.between(
						equipment.getLastDecreasedAt(),
						today
				);
				
			} else {
				//月単位
				passed = ChronoUnit.MONTHS.between(
						equipment.getLastDecreasedAt(),
						today
				);
				
			}

			// 設定した期間に達していなければ何もしない
			if (passed < equipment.getDecreaseInterval()) {
				continue;
			}

			// 何回分の減少が発生したか計算
			long decreaseCount = passed / equipment.getDecreaseInterval();

			// 合計で何個減らすか計算
			int totalDecrease = (int) (decreaseCount * equipment.getDecreaseQuantity());

			// 数量が0未満にならないようにする
			int newQuantity = Math.max(0, equipment.getQuantity() - totalDecrease);

			// 新しい数量を設定
			equipment.setQuantity(newQuantity);

			// 最後に減らした日、月を更新
			if("DAY".equals(equipment.getDecreaseUnit())) {
				
				equipment.setLastDecreasedAt(
						equipment.getLastDecreasedAt()
								.plusDays(
										decreaseCount * equipment.getDecreaseInterval()
								)
				);
				
			} else {
			
				equipment.setLastDecreasedAt(
						equipment.getLastDecreasedAt()
								.plusMonths(
										decreaseCount * equipment.getDecreaseInterval()
								)
				);
			
			}
			
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
	        @RequestParam(required = false) Integer decreaseInterval,
	        @RequestParam(required = false) Integer decreaseQuantity,
	        Model model) {
		
		//備品名に空白があった場合削除
		name = name.trim();
		

	    // 数量が1未満ならエラー
	    if (quantity < 1) {
	        model.addAttribute("error", "数量は1以上で入力してください。");
	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());
	        return "index";
	    }

	    // 減少間隔と減少数が片方だけ入力されている場合
	    if ((decreaseInterval == null && decreaseQuantity != null)
	            || (decreaseInterval != null && decreaseQuantity == null)) {

	        model.addAttribute(
	                "error",
	                "減少間隔と減少数は両方入力してください。"
	        );

	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());

	        return "index";
	    }

	    // 減少間隔が1未満ならエラー
	    if (decreaseInterval != null && decreaseInterval < 1) {
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
	    
	    //同じ名前の備品名を登録するとエラー
	    if (repository.existsByName(name)) {
	    	
	    	model.addAttribute(
	    			"errorMessage",
	    			"「" + name + "」はもう登録されています"
	    	);
	    	
	        model.addAttribute("list", repository.findAllByOrderByQuantityAsc());
	    	
	    	return "index";
	    	
	    }

	    Equipment equipment = new Equipment();

	    equipment.setName(name);
	    equipment.setQuantity(quantity);

	    // 自動減少の設定
	    equipment.setDecreaseInterval(decreaseInterval);
	    equipment.setDecreaseQuantity(decreaseQuantity);

	    // 自動減少を設定した場合だけ基準日を設定
	    if (decreaseInterval != null && decreaseQuantity != null) {

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

	//数量変更したときの処理
	@PostMapping("/quantity/update/{id}")
	public String updateQuantity(
			@PathVariable Integer id,
			@RequestParam Integer quantity,
			RedirectAttributes redirectAttributes) {
		
		Equipment equipment = repository.findById(id)
				.orElseThrow();
		
		//1未満になることを防ぐ
		if(quantity < 1) {
			quantity = 1;
		}
		
		equipment.setQuantity(quantity);
		
		//数量変更の通知
		redirectAttributes.addFlashAttribute(
				"message",
				"「" + equipment.getName() + "」の数量を"
				+ quantity + "個に変更しました"
		);
		
		repository.save(equipment);		
		
		return "redirect:/";
		
	}
	
	//減少設定の保存
	@PostMapping("/decrease-setting/{id}")
	public String setDecreaseSetting(
			@PathVariable Integer id,
			@RequestParam Integer decreaseInterval,
			@RequestParam String decreaseUnit,
			@RequestParam Integer decreaseQuantity,
			RedirectAttributes redirectAttributes) {
		
		//idから備品取得
		Equipment equipment = repository.findById(id)
				.orElseThrow();
		
		//減少間隔設定
		equipment.setDecreaseInterval(decreaseInterval);
		
		//日、月の設定
		equipment.setDecreaseUnit(decreaseUnit);
		
		//減少数設定
		equipment.setDecreaseQuantity(decreaseQuantity);
		
		//基準日の設定
		equipment.setLastDecreasedAt(LocalDate.now());
		
		//DBに保存
		repository.save(equipment);
		
		String unit;
		
		if("DAY".equals(decreaseUnit)) {
			unit = "日";
		} else {
			unit = "ヶ月";
		}
		
		redirectAttributes.addFlashAttribute(
				"message",
				"「" + equipment.getName()
				+ "」の自動減少を"
				+ decreaseInterval
				+ unit
				+ "ごとに"
				+ decreaseQuantity
				+"個減らすように設定しました"
		);
		
		return "redirect:/";
		
	}
	
	//減少設定の解除
	@PostMapping("/decrease-setting/delete/{id}")
	public String deleteDecreaseSetting(@PathVariable Integer id) {

	    Equipment equipment = repository.findById(id)
	            .orElseThrow();

	    equipment.setDecreaseInterval(null);
	    equipment.setDecreaseUnit(null);
	    equipment.setDecreaseQuantity(null);
	    equipment.setLastDecreasedAt(null);

	    repository.save(equipment);

	    return "redirect:/";
	}

}