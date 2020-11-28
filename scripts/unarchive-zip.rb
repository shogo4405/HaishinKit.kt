require 'zip'

if ARGV.size() == 0 then
  die
else
  filename = ARGV[0].to_s
end

dest = filename.gsub('.zip', '').concat('/')

Zip::File.open(filename) do |zip|
  zip.each do |entry|
    p entry.name
    # { true } は展開先に同名ファイルが存在する場合に上書きする指定
    zip.extract(entry, dest + entry.name) { true }
  end
end

Dir.chdir(dest)
Dir.glob("*.rgba") do |f|
    output = dest + f.to_s.gsub('.rgba', '.bmp')
    system "convert -flip -size 540x1044 -depth 8 RGBA:#{dest + f} -channel RGBA -separate -delete 3 -combine #{output}"
end

