package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class DemoApplicationTests {

	/*
	 * ① 1ヶ月経過したら2個減る
	 *
	 * 10個
	 * ↓ 1ヶ月経過
	 * 8個
	 */
	@Test
	void 一ヶ月経過したら数量が2個減る() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(10);
		equipment.setDecreaseInterval(1);
		equipment.setDecreaseQuantity(2);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 9, 16));

		assertEquals(8, equipment.getQuantity());

		verify(repository).save(equipment);
	}


	/*
	 * ② 1ヶ月経過していなければ減らない
	 *
	 * 10個
	 * ↓ 半月程度
	 * 10個
	 */
	@Test
	void 一ヶ月経過していなければ数量は減らない() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(10);
		equipment.setDecreaseInterval(1);
		equipment.setDecreaseQuantity(2);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 8, 30));

		assertEquals(10, equipment.getQuantity());

		// DB保存もされないことを確認
		verify(repository, never()).save(equipment);
	}


	/*
	 * ③ 3ヶ月経過したら3回分減る
	 *
	 * 1ヶ月ごとに2個減る設定
	 *
	 * 10個
	 * ↓ 3ヶ月
	 * 4個
	 */
	@Test
	void 三ヶ月経過したら六個減る() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(10);
		equipment.setDecreaseInterval(1);
		equipment.setDecreaseQuantity(2);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 11, 16));

		assertEquals(4, equipment.getQuantity());

		verify(repository).save(equipment);
	}


	/*
	 * ④ 数量が0未満にならない
	 *
	 * 1個しかない
	 * ↓ 2個減らす
	 * 0個
	 */
	@Test
	void 数量が0未満にならない() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(1);
		equipment.setDecreaseInterval(1);
		equipment.setDecreaseQuantity(2);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 9, 16));

		assertEquals(0, equipment.getQuantity());

		verify(repository).save(equipment);
	}


	/*
	 * ⑤ DecreaseIntervalが設定されていなければ何もしない
	 */
	@Test
	void 減少月数が未設定なら何もしない() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(10);

		// 減少月数を設定しない
		equipment.setDecreaseInterval(null);

		equipment.setDecreaseQuantity(2);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 9, 16));

		assertEquals(10, equipment.getQuantity());

		verify(repository, never()).save(equipment);
	}


	/*
	 * ⑥ 2ヶ月ごとに3個減らす
	 *
	 * 設定：
	 * 2ヶ月ごと
	 * 3個減らす
	 *
	 * 10個
	 * ↓ 4ヶ月
	 * 4個
	 */
	@Test
	void 二ヶ月ごとに三個減る() {

		EquipmentRepository repository = mock(EquipmentRepository.class);

		Equipment equipment = new Equipment();

		equipment.setQuantity(10);
		equipment.setDecreaseInterval(2);
		equipment.setDecreaseQuantity(3);
		equipment.setLastDecreasedAt(
				LocalDate.of(2026, 8, 16));

		when(repository.findAll())
				.thenReturn(List.of(equipment));

		EquipmentConsumptionService service =
				new EquipmentConsumptionService(repository);

		service.checkAndDecrease(
				LocalDate.of(2026, 12, 16));

		assertEquals(4, equipment.getQuantity());

		verify(repository).save(equipment);
	}
}