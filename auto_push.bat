@echo off
cd /d "C:\Users\nkhan\Web2_workspace\Car_Blog"
git remote set-url origin https://github.com/Ksdfs/Car_Blog.git
git add .
git commit -m "Auto commit at %date% %time%"
git push origin main
