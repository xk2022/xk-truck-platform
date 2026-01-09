二、application/port/in 會有哪些檔案（定稿版）

這一層 = 系統對外承諾的能力清單（Contract）
未來 UI / Batch / API / Message Consumer 都只能透過這層進來。

📁 com.xk.truck.tom.application.port.in
1️⃣ Command 型 UseCase（改變狀態）
CreateOrderUseCase
AcceptOrderUseCase
AssignOrderUseCase
ReassignOrderUseCase
CancelOrderUseCase
CloseOrderUseCase


每一個 只做一件事，避免上帝 UseCase。

範例：

public interface CreateOrderUseCase {
TomOrderResult create(CreateOrderCommand command);
}

2️⃣ Query 型 UseCase（只查詢，不改狀態）
FindOrderUseCase        // list / filter
GetOrderDetailUseCase  // detail


查詢一定獨立，未來可以：

換 projection

走 read DB

接 cache / ES
而不影響 command
