JAR = out/artifacts/Huffman_jar/Huffman.jar

all: $(JAR)

$(JAR):
	@echo "JAR-файл уже собран. Ничего не нужно делать."

compress:
	@echo "Сжатие файла..."
	java -jar $(JAR) compress $(word 2, $(MAKECMDGOALS)) $(word 3, $(MAKECMDGOALS))

decompress:
	@echo "Распаковка файла..."
	java -jar $(JAR) decompress $(word 2, $(MAKECMDGOALS)) $(word 3, $(MAKECMDGOALS))

clean:
	@echo "Очистка временных файлов..."
	rm -f *.huff *.txt

help:
	@echo "Доступные команды:"
	@echo "  make all      - проверка наличия JAR-файла"
	@echo "  make compress - сжать файл (input.txt -> output.huff)"
	@echo "  make extract  - распаковать файл (input.huff -> output.txt)"
	@echo "  make clean    - очистить временные файлы"
