"""CLI сборки глав диплома ВГТУ."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))


def main() -> None:
    parser = argparse.ArgumentParser(description="Сборка дипломных глав Project-A")
    sub = parser.add_subparsers(dest="command", required=True)

    ch = sub.add_parser("chapter", help="Собрать одну главу")
    ch.add_argument("--number", type=int, required=True, choices=[1, 2, 3])

    sub.add_parser("references", help="Собрать список литературы")
    sub.add_parser("introduction", help="Собрать введение")
    sub.add_parser("conclusion", help="Собрать заключение")
    sub.add_parser("merge", help="Собрать полный ВКР с содержанием")
    sub.add_parser("import-ch1", help="Импортировать главу 1 из исходного docx на рабочем столе")

    args = parser.parse_args()
    if args.command == "chapter":
        if args.number == 1:
            from build_chapter_1 import build
            print(build())
        elif args.number == 2:
            from build_chapter_2 import build
            print(build())
        elif args.number == 3:
            from build_chapter_3 import build
            print(build())
    elif args.command == "introduction":
        from build_introduction import build
        print(build())
    elif args.command == "references":
        from build_references import build
        print(build())
    elif args.command == "conclusion":
        from build_conclusion import build
        print(build())
    elif args.command == "merge":
        from build_merge import build
        print(build())
    elif args.command == "import-ch1":
        from import_chapter1_from_vkr import main
        print(main())
    else:
        raise SystemExit(f"Неизвестная команда: {args.command}")


if __name__ == "__main__":
    main()
