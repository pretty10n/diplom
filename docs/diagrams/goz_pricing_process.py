"""
BPMN: Процесс ценообразования ГОЗ (затратный метод).
Генерация PNG через библиотеку diagrams (требуется Graphviz).

  pip install diagrams
  # Graphviz: https://graphviz.org/download/

  python goz_pricing_process.py
"""
from diagrams import Diagram, Cluster, Edge
from diagrams.custom import Custom
from diagrams.onprem.workflow import Airflow
from diagrams.onprem.analytics import Superset
from diagrams.programming.language import Python

with Diagram(
    "BPMN: Процесс ценообразования ГОЗ (Затратный метод)",
    show=False,
    direction="TB",
    outformat="png",
    filename="goz_pricing_process",
):
    with Cluster("Головной исполнитель (Поставщик)"):
        with Cluster("Экономист / Калькулятор"):
            start = (
                Custom("Начало расчета", "./icons/start.png")
                if False
                else Airflow("Старт: Получение ТЗ")
            )
            rkm = Superset("Расчет калькуляции\n(Форма РКМ)")
            plan_fact = Superset("Плановые затраты\n(Материалы, ФОТ, ОПР/ОХР)")
            profit = Python("Расчет рентабельности\n(1% привнес. / 20% собств.)")

        with Cluster("Руководство"):
            approve = Airflow("Утверждение РКМ\nи передача Заказчику")

    start >> rkm >> plan_fact >> profit >> approve

    with Cluster("Заказчик (ГОЗ)"):
        with Cluster("Планово-экономический отдел"):
            receive = Airflow("Прием РКМ\nи Пояснительной записки")
            check = Superset(
                "Экономическая экспертиза\n(Проверка нормативов и индексов)"
            )

            with Cluster("Решение"):
                decision = Python("Цена корректна?\n(Соответствие 1465 ПП)")

            agree = Airflow("Согласование\nПротокола цены")
            reject = Airflow("Формирование\nПротокола разногласий")

        with Cluster("ВП МО РФ (Военное представительство)"):
            vp_check = Superset("Проверка обоснованности\nзатрат")
            fixed_price = Airflow("Перевод в ФИКСИРОВАННУЮ\nцену")

    approve >> Edge(label="Поток документов\n(РКМ, ПЗ)") >> receive
    receive >> check >> decision

    decision >> Edge(label="Да", color="darkgreen") >> agree
    decision >> Edge(label="Нет", color="red") >> reject

    reject >> Edge(label="Корректировка расчета", style="dashed", color="red") >> start

    agree >> Edge(label="На проверку", color="blue") >> vp_check
    vp_check >> Edge(label="Утверждение", color="darkgreen") >> fixed_price

    final_doc = (
        Custom("Протокол фиксированной\nцены подписан", "./icons/doc.png")
        if False
        else Airflow("Конец: Цена утверждена")
    )

    fixed_price >> final_doc
