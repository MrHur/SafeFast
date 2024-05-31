package org.zerock.safefast.controller.purchase_order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zerock.safefast.dto.purchase_order.PurchaseOrderRequest;
import org.zerock.safefast.entity.ProcurementPlan;
import org.zerock.safefast.entity.PurchaseOrder;
import org.zerock.safefast.repository.ProcurementPlanRepository;
import org.zerock.safefast.repository.PurchaseOrderRepository;
import org.zerock.safefast.service.procurement.ProcurementPlanService;
import org.zerock.safefast.service.purchase_order.PurchaseOrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/purchase_order")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseOrderController {

    @Autowired
    private ProcurementPlanRepository procurementPlanRepository;

    @Autowired
    private ProcurementPlanService procurementPlanService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public PurchaseOrderController(ProcurementPlanService procurementPlanService, PurchaseOrderService purchaseOrderService) {
        this.procurementPlanService = procurementPlanService;
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping("/purchase_order")
    public String createPurchaseOrder(@ModelAttribute PurchaseOrder purchaseOrder, Model model) {
        model.addAttribute("purchaseOrder", purchaseOrder);
        return "redirect:/purchase_order/purchase_order";
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPurchaseOrder(@RequestBody List<PurchaseOrderRequest> purchaseOrderRequests) {
        try {
            for (PurchaseOrderRequest request : purchaseOrderRequests) {
                ProcurementPlan procurementPlan = procurementPlanRepository.findById(request.getProcPlanNumber()).orElseThrow(() -> new IllegalArgumentException("Invalid procurement plan number"));

                String purchOrderNumber = purchaseOrderService.generateNextPurchOrderNumber();

                PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                        .purchOrderNumber(purchOrderNumber)
                        .purchOrderQuantity(request.getPurchOrderQuantity())
                        .receiveDuedate(request.getReceiveDuedate())
                        .procPlanNumber(request.getProcPlanNumber())
                        .purchOrderDate(request.getReceiveDuedate())
                        .coOpCompany(procurementPlan.getCoOpCompany())
                        .item(procurementPlan.getItem())
                        .build();

                purchaseOrderRepository.save(purchaseOrder);
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "Purchase orders created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create purchase orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getPlan")
    @ResponseBody
    public ProcurementPlan getProcurementPlan(@RequestParam String procPlanNumber) {
        return purchaseOrderService.getProcurementPlanByNumber(procPlanNumber)
                .orElseThrow(() -> new RuntimeException("조달계획 정보를 불러오지 못했습니다."));
    }

    @GetMapping("/purchase_order")
    public String showPurchaseOrderPage(Model model) {
        List<ProcurementPlan> procurementPlans = procurementPlanService.getAllProcurementPlans();
        model.addAttribute("procurementPlans", procurementPlans);
        return "purchase_order/purchase_order";
    }

    @GetMapping("/list")
    @ResponseBody
    public List<PurchaseOrder> getPurchaseOrderList() {
        return purchaseOrderService.getAllPurchaseOrders();
    }

    @GetMapping("/{purchOrderNumber}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable String purchOrderNumber) {
        Optional<PurchaseOrder> purchaseOrder = Optional.ofNullable(purchaseOrderRepository.findByPurchOrderNumber(purchOrderNumber));
        if (purchaseOrder.isPresent()) {
            return ResponseEntity.ok(purchaseOrder.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @PutMapping("/{purchOrderNumber}")
//    @Transactional
//    public ResponseEntity<String> updatePurchaseOrder(@PathVariable String purchOrderNumber, @RequestBody PurchaseOrderRequest request) {
//        try {
//            Optional<PurchaseOrder> optionalPurchaseOrder = Optional.ofNullable(purchaseOrderRepository.findByPurchOrderNumber(purchOrderNumber));
//
//            if (optionalPurchaseOrder.isPresent()) {
//                PurchaseOrder purchaseOrder = optionalPurchaseOrder.get();
//
//                // 업데이트할 데이터 설정
//                purchaseOrder.setPurchOrderQuantity(request.getPurchOrderQuantity());
//                purchaseOrder.setReceiveDuedate(request.getReceiveDuedate());
//                purchaseOrder.setPurchOrderDate(request.getPurchOrderDate());
//
//                // 업데이트된 발주서 저장
//                purchaseOrderRepository.save(purchaseOrder);
//
//                // 로그 출력
//                System.out.println("발주서가 성공적으로 업데이트되었습니다.");
//
//                return ResponseEntity.ok("발주서가 성공적으로 업데이트되었습니다.");
//            } else {
//                // 로그 출력
//                System.out.println("발주서가 발견되지 않았습니다.");
//
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            // 예외 로그 출력
//            e.printStackTrace();
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("발주서 업데이트 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }
@PutMapping("/{purchOrderNumber}")
public ResponseEntity<String> updatePurchaseOrder(@PathVariable String purchOrderNumber, @RequestBody PurchaseOrder modifiedOrder) {
    try {
        purchaseOrderService.updatePurchaseOrder(purchOrderNumber, modifiedOrder);
        return ResponseEntity.ok("발주서가 성공적으로 수정되었습니다.");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("발주서 수정 중 오류가 발생했습니다.");
    }
}
    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<String> deletePurchaseOrders(@RequestBody List<String> orderNumbers) {
        try {
            purchaseOrderRepository.deleteAllByPurchOrderNumberIn(orderNumbers);
            return ResponseEntity.ok("선택한 발주서가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("발주서 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
