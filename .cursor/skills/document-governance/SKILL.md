---
name: document-governance
description: Единый слой governance для Word-задач: source-of-truth по правилам, приоритеты при конфликтах, маршрутизация профилей документов. Use when multiple rules/skills overlap for diploma, academic reports, or technical documents.
---

# Document Governance

## Назначение

Этот скилл устраняет дубли и задает единый порядок принятия решений между правилами Word-задач.

## Приоритет правил

При конфликте использовать порядок:
1. Явная инструкция пользователя в текущей задаче.
2. Профильный source-of-truth для активного домена/профиля.
3. `alwaysApply` rules в `.cursor/rules/`.
4. Профильный скилл роли.
5. Общие рекомендации внутри вспомогательных скиллов.

Для Word-задач это означает:
- `diploma-vgtu` -> приоритет у `.cursor/skills/diploma-vgtu-md/SKILL.md`;
- `academic-report` -> приоритет у `.cursor/rules/ucheba-otchety-i-diagrammy.mdc`.

## Source Of Truth по темам

- **Дипломный Word-профиль:** `.cursor/skills/diploma-vgtu-md/SKILL.md`
- **Учебные отчеты/диаграммы:** `.cursor/rules/ucheba-otchety-i-diagrammy.mdc`
- **Маршрутизация Word-задач:** `.cursor/skills/word-worker/SKILL.md`

## Профили Word-задач

- `diploma-vgtu` -> применять `diploma-vgtu-md` как главный стандарт.
- `academic-report` -> применять `ucheba-otchety-i-diagrammy.mdc` как главный стандарт.
- `technical-doc` -> брать базовые правила форматирования из `word-worker` + уточнения пользователя.

Командный override:
- `/diploma-vgtu-md` принудительно активирует профиль `diploma-vgtu` для текущей задачи.

## Перед началом Word-задачи

Проверить:
1. Нет ли конфликта между правилами.
2. Какой профиль задачи активен.
3. Какой документ является source-of-truth для этого профиля.
