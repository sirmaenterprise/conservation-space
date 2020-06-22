{
	string = $0;
	start = index(string, "${");

	while(start > 0) {
		end = index(string, "}");
		if(end > 0) {
			variable = substr(string, start + 2, end - start - 2);
			evaluated = ENVIRON[variable];
			if(length(evaluated) > 0) {
				toreplace = "[$]{"variable"}";
				gsub(toreplace, evaluated, $0);
			}
			string = substr(string, end + 1);
			start = index(string, "${");
		} else {
			string = substr(string, start + 2);
			start = index(string, "${");
		}
	}
	print $0
}
